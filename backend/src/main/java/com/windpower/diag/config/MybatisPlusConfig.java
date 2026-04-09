package com.windpower.diag.config;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.data.annotation.Ds;

@Configuration
public class MybatisPlusConfig {

    @Bean
    public void db1Cfg(@Ds("db1") MybatisConfiguration cfg) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        cfg.addInterceptor(interceptor);
    }
}
