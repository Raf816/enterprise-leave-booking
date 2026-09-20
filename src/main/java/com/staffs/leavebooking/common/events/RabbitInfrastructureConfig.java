package com.staffs.leavebooking.common.events;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitInfrastructureConfig {

    @Bean
    public TopicExchange staffManagementExchange() {
        return new TopicExchange("staff-management");
    }

    @Bean
    public TopicExchange leaveNotificationsExchange() {
        return new TopicExchange("leave-notifications");
    }

    @Bean
    public Queue staffMemberAddedQueue() {
        return QueueBuilder.durable("leave-management.staff-member-added").build();
    }

    @Bean
    public Queue staffMemberUpdatedQueue() {
        return QueueBuilder.durable("leave-management.staff-member-updated").build();
    }

    @Bean
    public Queue managerNotificationQueue() {
        return QueueBuilder.durable("notifications.manager-pending-request").build();
    }

    @Bean
    public Queue staffNotificationQueue() {
        return QueueBuilder.durable("notifications.staff-request-decided").build();
    }

    @Bean
    public Binding bindStaffMemberAdded(Queue staffMemberAddedQueue,
                                         TopicExchange staffManagementExchange) {
        return BindingBuilder
                .bind(staffMemberAddedQueue)
                .to(staffManagementExchange)
                .with("staff.member.added");
    }

    @Bean
    public Binding bindStaffMemberUpdated(Queue staffMemberUpdatedQueue,
                                           TopicExchange staffManagementExchange) {
        return BindingBuilder
                .bind(staffMemberUpdatedQueue)
                .to(staffManagementExchange)
                .with("staff.member.updated");
    }

    @Bean
    public Binding bindManagerNotification(Queue managerNotificationQueue,
                                            TopicExchange leaveNotificationsExchange) {
        return BindingBuilder
                .bind(managerNotificationQueue)
                .to(leaveNotificationsExchange)
                .with("notification.manager.pending");
    }

    @Bean
    public Binding bindStaffNotification(Queue staffNotificationQueue,
                                          TopicExchange leaveNotificationsExchange) {
        return BindingBuilder
                .bind(staffNotificationQueue)
                .to(leaveNotificationsExchange)
                .with("notification.staff.decided");
    }
}
