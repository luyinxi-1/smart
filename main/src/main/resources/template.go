package main

import (
	"bytes"
	"fmt"
	"github.com/ncruces/zenity"
	"os/exec"
	"runtime"
	"strings"
)

// === 这些占位符将被 Java 程序动态替换 ===
const presetDeviceCode = "DEVICE_CODE_PLACEHOLDER"
const zipPassword = "PASSWORD_PLACEHOLDER"
// =====================================

// getDeviceUUID 尝试获取跨平台的设备UUID
func getDeviceUUID() (string, error) {
	switch runtime.GOOS {
	case "windows":
		// 优先使用 PowerShell
		cmd := exec.Command("powershell", "-Command", "(Get-CimInstance Win32_ComputerSystemProduct).UUID")
		var out bytes.Buffer
		cmd.Stdout = &out
		if err := cmd.Run(); err == nil {
			uuid := strings.TrimSpace(out.String())
			if uuid != "" {
				return uuid, nil
			}
		}
		// 回退到旧版 wmic
		cmd = exec.Command("wmic", "csproduct", "get", "uuid")
		out.Reset()
		cmd.Stdout = &out
		if err := cmd.Run(); err != nil {
			return "", fmt.Errorf("无法通过 PowerShell 或 wmic 获取UUID: %v", err)
		}
		lines := strings.Split(out.String(), "\n")
		if len(lines) >= 2 {
			return strings.TrimSpace(lines[1]), nil
		}
		return "", fmt.Errorf("无法解析UUID输出")

	case "darwin":
		return "", fmt.Errorf("macOS尚未支持")
	case "linux":
		return "", fmt.Errorf("Linux尚未支持")
	default:
		return "", fmt.Errorf("不支持的操作系统: %s", runtime.GOOS)
	}
}

// 复制到剪贴板（仅 Windows，使用系统自带 clip.exe）
func copyToClipboard(text string) error {
	if runtime.GOOS != "windows" {
		return fmt.Errorf("当前系统不支持自动复制到剪贴板（GOOS=%s）", runtime.GOOS)
	}

	// 方式一：直接调用 clip，让 stdin 的内容进剪贴板
	cmd := exec.Command("cmd", "/C", "clip")
	cmd.Stdin = strings.NewReader(text)

	if err := cmd.Run(); err != nil {
		return fmt.Errorf("调用 clip 失败: %v", err)
	}
	return nil
}

func main() {
	// 获取当前设备的ID
	localDeviceCode, err := getDeviceUUID()
	if err != nil {
		zenity.Error(
			fmt.Sprintf("无法获取设备ID: %v", err),
			zenity.Title("验证失败"),
			zenity.ErrorIcon)
		return
	}

	// 对比设备ID
	if localDeviceCode != presetDeviceCode {
		zenity.Error(
			fmt.Sprintf("此程序无法在当前设备上运行。\n\n程序设备码: %s\n本地设备码: %s", presetDeviceCode, localDeviceCode),
			zenity.Title("验证失败"),
			zenity.ErrorIcon)
		return
	}

	// 设备验证成功，先尝试自动复制密码
	if err := copyToClipboard(zipPassword); err != nil {
		// 复制失败，就提示用户手动记录
		zenity.Warning(
			fmt.Sprintf("设备验证成功，但自动复制密码失败：\n%v\n\n请手动记下此密码：\n\n%s", err, zipPassword),
			zenity.Title("验证成功（复制失败）"),
		)
		return
	}

	// 复制成功，弹窗提示 + 显示密码
	zenity.Info(
		fmt.Sprintf("设备验证成功！\n\n解压密码已自动复制到剪贴板：\n\n%s\n\n现在可以直接 Ctrl+V 粘贴使用。", zipPassword),
		zenity.Title("验证成功"),
		zenity.InfoIcon)
}
