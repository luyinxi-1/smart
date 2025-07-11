package com.upc.config.wordToHtmlTool;

import com.aspose.words.License;
import javax.annotation.PostConstruct; // for Spring Boot 2.x
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;

@Configuration
public class AsposeLicenseConfig {

    @PostConstruct
    public void setLicense() {
        try {
            // 许可证文件名，确保它在 src/main/resources 目录下
            String licenseFile = "Aspose.Words.Java.lic";

            // 从类路径加载许可证文件流
            InputStream licenseStream = AsposeLicenseConfig.class.getClassLoader().getResourceAsStream(licenseFile);

            if (licenseStream == null) {
                System.err.println("!!! Cannot find license file " + licenseFile + ". Aspose.Words will run in evaluation mode.");
                return;
            }

            // 创建并设置许可证
            License license = new License();
            license.setLicense(licenseStream);

            System.out.println("Aspose.Words license has been set successfully.");
        } catch (Exception e) {
            System.err.println("!!! Error setting Aspose.Words license. Aspose.Words will run in evaluation mode.");
            e.printStackTrace();
        }
    }
}