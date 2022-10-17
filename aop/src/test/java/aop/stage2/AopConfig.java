package aop.stage2;

import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import aop.stage1.TransactionAdvice;
import aop.stage1.TransactionAdvisor;
import aop.stage1.TransactionPointcut;

@Configuration
public class AopConfig {

    private final PlatformTransactionManager transactionManager;

    public AopConfig(final PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Bean
    public TransactionAdvisor getTransactionAdvisor() {
        return new TransactionAdvisor(new TransactionAdvice(transactionManager), new TransactionPointcut());
    }

    @Bean
    public DefaultAdvisorAutoProxyCreator getAdvisorAutoProxyCreator() {
        return new DefaultAdvisorAutoProxyCreator();
    }
}
