// src/main/java/com/example/swp/dto/NewStaffForm.java
package com.example.swp.dto;

import com.example.swp.enums.RoleName;
import jakarta.validation.constraints.*;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewStaffForm {

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 255, message = "Họ tên tối đa 255 ký tự")
    private String fullname;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "SĐT không được để trống")
    @Pattern(regexp = "\\d{10,11}", message = "SĐT phải gồm 10–11 chữ số")
    @Size(max = 20, message = "SĐT tối đa 20 ký tự")
    private String phone;

    @NotNull(message = "Giới tính là bắt buộc")
    private Boolean sex;

    // Optional – default to STAFF if null
    private RoleName roleName;
}
