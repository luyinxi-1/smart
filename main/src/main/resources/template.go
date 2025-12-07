package main

import (
	"bytes"
	"fmt"
	"github.com/ncruces/zenity"
	"os"
	"os/exec"
	"runtime"
	"strings"
)

// === 这些占位符将被 Java 程序动态替换 ===
const presetDeviceCode = "DEVICE_CODE_PLACEHOLDER"
const zipPassword = "PASSWORD_PLACEHOLDER"
// =====================================

// -------- 公共的弹窗封装：GUI 失败时退回到控制台 --------

func showError(msg, title string) {
	if err := zenity.Error(msg, zenity.Title(title), zenity.ErrorIcon); err != nil {
		fmt.Println("[ERROR]", title, msg)
	}
}

func showWarning(msg, title string) {
	if err := zenity.Warning(msg, zenity.Title(title)); err != nil {
		fmt.Println("[WARN]", title, msg)
	}
}

func showInfo(msg, title string) {
	if err := zenity.Info(msg, zenity.Title(title), zenity.InfoIcon); err != nil {
		fmt.Println("[INFO]", title, msg)
	}
}

// -------- 设备 ID 获取：Windows + Linux (麒麟/aarch64 优先) --------

// getDeviceUUID 尝试获取“设备唯一 ID”
func getDeviceUUID() (string, error) {
	switch runtime.GOOS {
	case "windows":
		// 1) 优先使用 PowerShell
		{
			cmd := exec.Command("powershell", "-Command", "(Get-CimInstance Win32_ComputerSystemProduct).UUID")
			var out bytes.Buffer
			cmd.Stdout = &out
			if err := cmd.Run(); err == nil {
				uuid := strings.TrimSpace(out.String())
				if uuid != "" {
					return uuid, nil
				}
			}
		}

		// 2) 回退到旧版 wmic
		{
			cmd := exec.Command("wmic", "csproduct", "get", "uuid")
			var out bytes.Buffer
			cmd.Stdout = &out
			if err := cmd.Run(); err != nil {
				return "", fmt.Errorf("无法通过 PowerShell 或 wmic 获取UUID: %v", err)
			}
			lines := strings.Split(out.String(), "\n")
			if len(lines) >= 2 {
				id := strings.TrimSpace(lines[1])
				if id != "" {
					return id, nil
				}
			}
			return "", fmt.Errorf("无法解析UUID输出")
		}

	case "linux":
		// ---- 国产 Linux / 麒麟桌面 优先路径 ----
		// 1) /etc/machine-id
		if b, err := os.ReadFile("/etc/machine-id"); err == nil {
			id := strings.TrimSpace(string(b))
			if id != "" && id != "00000000000000000000000000000000" {
				return id, nil
			}
		}

		// 2) 有些系统用 /var/lib/dbus/machine-id
		if b, err := os.ReadFile("/var/lib/dbus/machine-id"); err == nil {
			id := strings.TrimSpace(string(b))
			if id != "" {
				return id, nil
			}
		}

		// 3) 尝试 DMI 的 product_uuid（部分国产机也有）
		if b, err := os.ReadFile("/sys/class/dmi/id/product_uuid"); err == nil {
			id := strings.TrimSpace(string(b))
			if id != "" && id != "00000000-0000-0000-0000-000000000000" {
				return id, nil
			}
		}

		// 4) 如果有 hostnamectl，再试 Machine ID
		if _, err := exec.LookPath("hostnamectl"); err == nil {
			cmd := exec.Command("hostnamectl")
			var out bytes.Buffer
			cmd.Stdout = &out
			if err := cmd.Run(); err == nil {
				for _, line := range strings.Split(out.String(), "\n") {
					if strings.Contains(line, "Machine ID") {
						parts := strings.SplitN(line, ":", 2)
						if len(parts) == 2 {
							id := strings.TrimSpace(parts[1])
							if id != "" {
								return id, nil
							}
						}
					}
				}
			}
		}

		return "", fmt.Errorf("无法在 Linux 上获取稳定的设备ID")

	default:
		return "", fmt.Errorf("不支持的操作系统: %s", runtime.GOOS)
	}
}

// -------- 剪贴板：Windows + 简单 Linux 支持，失败则让用户手动复制 --------

// copyToClipboard 尝试复制到剪贴板，失败返回 error，由上层决定如何提示
func copyToClipboard(text string) error {
	switch runtime.GOOS {
	case "windows":
		cmd := exec.Command("cmd", "/C", "clip")
		cmd.Stdin = strings.NewReader(text)
		if err := cmd.Run(); err != nil {
			return fmt.Errorf("调用 clip 失败: %v", err)
		}
		return nil

	case "linux":
		// 1) xclip
		if _, err := exec.LookPath("xclip"); err == nil {
			cmd := exec.Command("xclip", "-selection", "clipboard")
			cmd.Stdin = strings.NewReader(text)
			if err := cmd.Run(); err == nil {
				return nil
			}
		}

		// 2) xsel
		if _, err := exec.LookPath("xsel"); err == nil {
			cmd := exec.Command("xsel", "--clipboard", "--input")
			cmd.Stdin = strings.NewReader(text)
			if err := cmd.Run(); err == nil {
				return nil
			}
		}

		// 3) Wayland: wl-copy
		if _, err := exec.LookPath("wl-copy"); err == nil {
			cmd := exec.Command("wl-copy")
			cmd.Stdin = strings.NewReader(text)
			if err := cmd.Run(); err == nil {
				return nil
			}
		}

		return fmt.Errorf("未检测到 xclip/xsel/wl-copy，无法自动复制到剪贴板")

	default:
		return fmt.Errorf("当前系统不支持自动复制到剪贴板（GOOS=%s）", runtime.GOOS)
	}
}

// -------- 主逻辑 --------

func main() {
	// 1. 获取当前设备的 ID
	localDeviceCode, err := getDeviceUUID()
	if err != nil {
		showError(fmt.Sprintf("无法获取设备ID: %v", err), "验证失败")
		return
	}

	// 2. 对比设备 ID
	if localDeviceCode != presetDeviceCode {
		showError(
			fmt.Sprintf("此程序无法在当前设备上运行。\n\n程序设备码: %s\n本地设备码: %s", presetDeviceCode, localDeviceCode),
			"验证失败",
		)
		return
	}

	// 3. 设备验证成功，尝试自动复制密码
	if err := copyToClipboard(zipPassword); err != nil {
		// 自动复制失败：给一个可编辑的输入框，方便 Ctrl+C
		_, _ = zenity.Entry(
			fmt.Sprintf("设备验证成功，但自动复制密码失败：\n%v\n\n请使用 Ctrl+C 复制下面的密码：", err),
			zenity.Title("验证成功（请手动复制）"),
			zenity.EntryText(zipPassword),
		)
		return
	}

	// 4. 自动复制成功，正常提示
	showInfo(
		fmt.Sprintf("设备验证成功！\n\n解压密码已自动复制到剪贴板：\n\n%s\n\n现在可以直接粘贴使用。", zipPassword),
		"验证成功",
	)
}
