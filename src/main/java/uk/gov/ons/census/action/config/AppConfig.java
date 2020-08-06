package uk.gov.ons.census.action.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.TimeZone;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.transaction.RabbitTransactionManager;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.transaction.ChainedTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableScheduling
@EnableTransactionManagement
public class AppConfig {
  @PostConstruct
  public void init() {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @Bean
  public RabbitTemplate rabbitTemplate(
      ConnectionFactory connectionFactory, Jackson2JsonMessageConverter messageConverter) {
    RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
    rabbitTemplate.setMessageConverter(messageConverter);
    rabbitTemplate.setChannelTransacted(true);
    return rabbitTemplate;
  }

  @Bean
  public Jackson2JsonMessageConverter messageConverter() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    return new Jackson2JsonMessageConverter(objectMapper);
  }

  @Bean
  public AmqpAdmin amqpAdmin(ConnectionFactory connectionFactory) {
    return new RabbitAdmin(connectionFactory);
  }

  @Bean
  public PlatformTransactionManager transactionManager(
      EntityManagerFactory emf, ConnectionFactory connectionFactory) {
    JpaTransactionManager jpaTransactionManager = new JpaTransactionManager(emf);
    RabbitTransactionManager rabbitTransactionManager =
        new RabbitTransactionManager(connectionFactory);

    // We are using a technique described by Dr David Syer in order to synchronise the commits
    // and rollbacks across both Rabbit and Postgres (i.e. AMQP and JPA/Hibernate/JDBC).
    // We could have used Atomikos, but it was decided to be overkill by architects & tech leads.
    // The original article is: Distributed transactions in Spring, with and without XA
    // infoworld.com/article/2077963/distributed-transactions-in-spring--with-and-without-xa.html
    return new ChainedTransactionManager(rabbitTransactionManager, jpaTransactionManager);
  }
}
