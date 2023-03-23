package com.yupi.usercenter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

/**
 * 自定义版 Knife4j 组件配置文件.
 */
@Configuration
@EnableSwagger2WebMvc
@Profile("dev")
public class SwaggerConfig {

    @Bean(value = "defaultApi2")
    public Docket defaultApi2() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                //分组名称
                .groupName("2.X版本")
                .select()
                //这里指定Controller扫描包路径
                .apis(RequestHandlerSelectors.basePackage("com.yupi.usercenter"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return (new ApiInfoBuilder()
                //.title("swagger-bootstrap-ui-demo RESTful APIs")
                .description("# swagger-bootstrap-ui-demo RESTful APIs")
                .termsOfServiceUrl("http://doc.xiaominfo.com/docs/action/springboot")
                .contact(new Contact("xx@qq.com", "https://doc.xiaominfo.com/docs/action/springboot", "1686020078@qq.com"))
                .version("1.0")
                .build());
    }
}
