package br.ufscar.streaming.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.util.Locale;

/**
 * Configuração da camada de apresentação MVC e suporte bilíngue (Português e Inglês).
 *
 * @version 2.0.0
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * Define o resolvedor de idioma baseado em cookie com padrão em Português do Brasil.
     *
     * @return instância configurada de {@link LocaleResolver}
     */
    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver("ONDESONORA_LANG");
        resolver.setDefaultLocale(Locale.forLanguageTag("pt-BR"));
        return resolver;
    }

    /**
     * Intercepta o parâmetro de requisição {@code lang} para alternância dinâmica entre PT e EN.
     *
     * @return interceptador de mudança de localidade
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}
