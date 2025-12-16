package main

import (
	"bufio"
	"bytes"
	"fmt"
	"github.com/ncruces/zenity"
	"os"
	"os/exec"
	"runtime"
	"strings"
)

// === 这些占位符将被 Java 程序动态替换 ===
const presetDeviceCodes = `DEVICE_CODE_PLACEHOLDER`
const zipPassword = "PASSWORD_PLACEHOLDER"
// =====================================

// 获取当前设备的唯一标识
func getDeviceUUID() (string, error) {
	switch runtime.GOOS {
	case "windows":
		// 1) PowerShell 优先
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
		// 2) 回退到 wmic
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
		// 1) /etc/machine-id
		if b, err := os.ReadFile("/etc/machine-id"); err == nil {
			id := strings.TrimSpace(string(b))
			if id != "" && id != "00000000000000000000000000000000" {
				return id, nil
			}
		}
		// 2) /var/lib/dbus/machine-id
		if b, err := os.ReadFile("/var/lib/dbus/machine-id"); err == nil {
			id := strings.TrimSpace(string(b))
			if id != "" {
				return id, nil
			}
		}
		// 3) product_uuid
		if b, err := os.ReadFile("/sys/class/dmi/id/product_uuid"); err == nil {
			id := strings.TrimSpace(string(b))
			if id != "" && id != "00000000-0000-0000-0000-000000000000" {
				return id, nil
			}
		}
		return "", fmt.Errorf("无法在 Linux 上获取稳定的设备ID")

	default:
		return "", fmt.Errorf("不支持的操作系统: %s", runtime.GOOS)
	}
}

// Windows 下自动复制到剪贴板
func copyToClipboard(text string) error {
	if runtime.GOOS != "windows" {
		return fmt.Errorf("当前系统不支持自动复制到剪贴板")
	}
	cmd := exec.Command("cmd", "/C", "clip")
	cmd.Stdin = strings.NewReader(text)
	if err := cmd.Run(); err != nil {
		return fmt.Errorf("调用 clip 失败: %v", err)
	}
	return nil
}

// Linux 下终端暂停，避免窗口一闪而过
func pauseForEnter() {
	fmt.Println("\n按回车键退出...")
	reader := bufio.NewReader(os.Stdin)
	_, _ = reader.ReadString('\n')
}

// Windows 逻辑：图形弹窗 + 自动复制
func runWindows() {
	localDeviceCode, err := getDeviceUUID()
	if err != nil {
		zenity.Error(
			fmt.Sprintf("无法获取设备ID: %v", err),
			zenity.Title("验证失败"),
		)
		return
	}

	// 解析允许的设备码列表
	allowed := make(map[string]bool)
	for _, code := range strings.FieldsFunc(presetDeviceCodes, func(r rune) bool {
		return strings.ContainsRune(",;\n\r\t ", r)
	}) {
		trimmed := strings.TrimSpace(code)
		if trimmed != "" {
			allowed[trimmed] = true
		}
	}

	if !allowed[localDeviceCode] {
		zenity.Error(
			fmt.Sprintf("此程序无法在当前设备上运行。\n\n允许设备数量: %d\n本地设备码: %s",
				len(allowed), localDeviceCode),
			zenity.Title("验证失败"),
		)
		return
	}

	// 设备码匹配，尝试复制密码
	if err := copyToClipboard(zipPassword); err != nil {
		zenity.Warning(
			fmt.Sprintf("设备验证成功，但自动复制密码失败：\n%v\n\n请手动记下此密码：\n\n%s",
				err, zipPassword),
			zenity.Title("验证成功（复制失败）"),
		)
		return
	}

	zenity.Info(
		fmt.Sprintf("设备验证成功！\n\n解压密码已自动复制到剪贴板：\n\n%s\n\n现在可以直接粘贴使用。",
			zipPassword),
		zenity.Title("验证成功"),
		zenity.InfoIcon,
	)
}

// Linux 逻辑：纯命令行输出
func runLinux() {
	localDeviceCode, err := getDeviceUUID()
	if err != nil {
		fmt.Println("[验证失败] 无法获取设备ID：", err)
		pauseForEnter()
		return
	}

	fmt.Println("当前设备码：", localDeviceCode)

	// 解析允许的设备码列表
	allowed := make(map[string]bool)
	for _, code := range strings.FieldsFunc(presetDeviceCodes, func(r rune) bool {
		return strings.ContainsRune(",;\n\r\t ", r)
	}) {
		trimmed := strings.TrimSpace(code)
		if trimmed != "" {
			allowed[trimmed] = true
		}
	}

	if !allowed[localDeviceCode] {
		fmt.Println("\n[验证失败] 此程序无法在当前设备上运行。")
		fmt.Printf("允许设备数量: %d\n本地设备码: %s\n", len(allowed), localDeviceCode)
		pauseForEnter()
		return
	}

	fmt.Println("\n[验证成功]")
	fmt.Println("解压密码如下，请手动复制保存：")
	fmt.Println(zipPassword)
	pauseForEnter()
}

func main() {
	switch runtime.GOOS {
	case "windows":
		runWindows()
	case "linux":
		runLinux()
	default:
		fmt.Println("暂不支持当前操作系统：", runtime.GOOS)
	}
}
