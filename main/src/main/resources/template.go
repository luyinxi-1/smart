package main

import (
	"bytes"
	"fmt"
	"github.com/ncruces/zenity"
	"os/exec"
	"runtime"
	"strings"
)

// === 这些占位符将被Java程序动态替换 ===
const presetDeviceCode = "DEVICE_CODE_PLACEHOLDER"
const zipPassword = "PASSWORD_PLACEHOLDER"
// =====================================

// getDeviceUUID 尝试获取跨平台的设备UUID
func getDeviceUUID() (string, error) {
	switch runtime.GOOS {
	case "windows":
		cmd := exec.Command("wmic", "csproduct", "get", "uuid")
		var out bytes.Buffer
		cmd.Stdout = &out
		if err := cmd.Run(); err != nil {
			return "", err
		}
		lines := strings.Split(out.String(), "\n")
		if len(lines) >= 2 {
			return strings.TrimSpace(lines[1]), nil
		}
		return "", fmt.Errorf("无法解析wmic输出")
	// 您可以根据需要添加macOS和Linux的实现
	case "darwin":
		return "", fmt.Errorf("macOS尚未支持")
	case "linux":
		return "", fmt.Errorf("Linux尚未支持")
	default:
		return "", fmt.Errorf("不支持的操作系统: %s", runtime.GOOS)
	}
}

func main() {
	// 获取当前设备的ID
	localDeviceCode, err := getDeviceUUID()
	if err != nil {
		// 如果获取失败，弹出错误提示
		zenity.Error(
			fmt.Sprintf("无法获取设备ID: %v", err),
			zenity.Title("验证失败"),
			zenity.ErrorIcon)
		return
	}

	// 对比设备ID
	if localDeviceCode == presetDeviceCode {
		// 如果成功，弹出信息框显示密码
		zenity.Info(
			fmt.Sprintf("设备验证成功！\n\n您的解压密码是: %s", zipPassword),
			zenity.Title("验证成功"),
			zenity.InfoIcon)
	} else {
		// 如果失败，弹出错误提示
		zenity.Error(
			fmt.Sprintf("此程序无法在当前设备上运行。\n\n程序设备码: %s\n本地设备码: %s", presetDeviceCode, localDeviceCode),
			zenity.Title("验证失败"),
			zenity.ErrorIcon)
	}
}