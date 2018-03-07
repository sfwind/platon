package com.iquanwai.platon.web.swagger;

import com.iquanwai.platon.web.resolver.GuestUser;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.resolver.UnionUser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Created by justin on 2018/3/2.
 */
@EnableSwagger2
@Configuration
@ComponentScan(basePackages = "com.iquanwai")
public class SwaggerConfig {
    @Bean
    public Docket buildDocket() {
        return new Docket(DocumentationType.SWAGGER_2)
                .ignoredParameterTypes(LoginUser.class, UnionUser.class, GuestUser.class)
                .apiInfo(buildApiInfo()).select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any()).build();
    }

    @SuppressWarnings("deprecation")
    private ApiInfo buildApiInfo() {
        return new ApiInfoBuilder()
                .title("圈外同学移动端api文档")
                .description("api接口说明")
                .termsOfServiceUrl("https://www.iquanwai.com/")
                .version("1.0")
                .build();
    }
}
