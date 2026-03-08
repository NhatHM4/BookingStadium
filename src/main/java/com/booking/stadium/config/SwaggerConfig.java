package com.booking.stadium.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI bookingStadiumOpenAPI() {
        final String securitySchemeName = "Bearer Authentication";

        return new OpenAPI()
                .info(new Info()
                        .title("Booking Stadium API")
                        .description("Hệ thống đặt sân bóng đá trực tuyến - REST API Documentation.\n\n"
                                + "## Tính năng chính\n"
                                + "- **Authentication**: Đăng ký, đăng nhập, JWT token\n"
                                + "- **Stadium Management**: CRUD sân bóng, duyệt sân (Admin)\n"
                                + "- **Field & TimeSlot**: Quản lý sân con và khung giờ\n"
                                + "- **Booking**: Đặt sân, xác nhận, hủy, hoàn thành\n"
                                + "- **Deposit**: Đặt cọc, xác nhận, hoàn cọc\n"
                                + "- **Recurring Booking**: Đặt sân dài hạn (tuần/tháng)\n"
                                + "- **Team**: Quản lý đội bóng, mời thành viên\n"
                                + "- **Match Making**: Tạo kèo, nhận kèo, ráp đối\n"
                                + "- **Review**: Đánh giá sân sau khi hoàn thành\n"
                                + "- **Admin**: Quản lý users, dashboard thống kê\n\n"
                                + "## Roles\n"
                                + "- `CUSTOMER` - Khách hàng đặt sân\n"
                                + "- `OWNER` - Chủ sân quản lý sân bóng\n"
                                + "- `ADMIN` - Quản trị hệ thống\n\n"
                                + "## Test Accounts\n"
                                + "| Email | Password | Role |\n"
                                + "|-------|----------|------|\n"
                                + "| customer@example.com | 123456 | CUSTOMER |\n"
                                + "| owner@example.com | 123456 | OWNER |\n"
                                + "| admin@example.com | 123456 | ADMIN |")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Booking Stadium Team")
                                .email("support@bookingstadium.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server")))
                .addSecurityItem(new SecurityRequirement()
                        .addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Nhập JWT token (không cần prefix 'Bearer '). "
                                                + "Lấy token bằng API đăng nhập POST /api/v1/auth/login")));
    }
}
