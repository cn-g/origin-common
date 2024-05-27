package com.gcp.common.swagger.config;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Value;
import springfox.documentation.builders.PathSelectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.Contact;
import springfox.documentation.service.SecurityScheme;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

@Configuration
@EnableSwagger2WebMvc
public class SwaggerConfiguration {

    @Value("${chineseName}")
    private String applicationName;

    @Value("${basePackage}")
    private String basePackage;

    @Bean
    public Docket pubApi() {
        String path = basePackage+".controller.pub";
        return new Docket(DocumentationType.SWAGGER_2).groupName(applicationName+"公共Api").apiInfo(apiInfo()).select()
                .apis(RequestHandlerSelectors.basePackage(path)).paths(PathSelectors.any()).build()
                .securitySchemes(Lists.<SecurityScheme>newArrayList(tokenKey(), accessType()));
    }

    /**
     * 后台相关API
     * @return
     */
    @Bean
    public Docket centerApi() {
        return new Docket(DocumentationType.SWAGGER_2).groupName(applicationName+"后台相关Api").apiInfo(apiInfo()).select()
                .apis(RequestHandlerSelectors.basePackage(basePackage+".controller.center")).paths(PathSelectors.any()).build()
                .securitySchemes(Lists.<SecurityScheme>newArrayList(tokenKey(), accessType()));
    }

    @Bean
    public Docket feignApi() {
        return new Docket(DocumentationType.SWAGGER_2).groupName(applicationName+"feign相关Api").apiInfo(apiInfo()).select()
                .apis(RequestHandlerSelectors.basePackage(basePackage+".api.feign")).paths(PathSelectors.any()).build()
                .securitySchemes(Lists.<SecurityScheme>newArrayList(tokenKey(), accessType()));
    }
    private ApiKey tokenKey() {
        return new ApiKey("用户认证", "token", "header");
    }

    private ApiKey accessType() {
        return new ApiKey("用户角色id", "role_id", "header");
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder().title(applicationName+"相关Api").description("swagger APIS")
                .contact(new Contact("gcp", "https://github.com/cn-g", "2046039989@qq.com")).version("1.0.0").build();
    }

}
