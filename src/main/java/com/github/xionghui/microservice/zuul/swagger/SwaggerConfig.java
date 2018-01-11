package com.github.xionghui.microservice.zuul.swagger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Swagger2配置(http://localhost:port/swagger-ui.html)
 *
 * @author xionghui
 * @version 1.0.0
 * @since 1.0.0
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

  @Bean
  public Docket api() {
    return new Docket(DocumentationType.SWAGGER_2).groupName("Api").apiInfo(this.apiInfo()).select()
        .apis(
            RequestHandlerSelectors.basePackage("com.github.xionghui.microservice.zuul.controller"))
        .paths(PathSelectors.any()).build();
  }

  private ApiInfo apiInfo() {
    return new ApiInfoBuilder().title("微服务Api").description("微服务Api设计文档").version("1.0.0")
        .termsOfServiceUrl("https://github.com/xionghui-microservice")
        .contact(new Contact("熊辉", "https://github.com/xionghuiCoder", "xionghui_coder@163.com"))
        .build();
  }
}
