package br.com.cardoso.configuration;

import io.jaegertracing.internal.JaegerTracer;
import io.jaegertracing.internal.metrics.Metrics;
import io.jaegertracing.internal.metrics.NoopMetricsFactory;
import io.jaegertracing.internal.reporters.CompositeReporter;
import io.jaegertracing.internal.reporters.LoggingReporter;
import io.jaegertracing.internal.samplers.*;
import io.jaegertracing.spi.MetricsFactory;
import io.jaegertracing.spi.Reporter;
import io.jaegertracing.spi.Sampler;
import io.jaegertracing.spi.Sender;
import io.opentracing.contrib.java.spring.jaeger.starter.JaegerConfigurationProperties;
import io.opentracing.contrib.java.spring.jaeger.starter.ReporterAppender;
import io.opentracing.contrib.java.spring.jaeger.starter.TracerBuilderCustomizer;
import io.opentracing.contrib.java.spring.jaeger.starter.customizers.B3CodecTracerBuilderCustomizer;
import io.opentracing.contrib.java.spring.jaeger.starter.customizers.ExpandExceptionLogsTracerBuilderCustomizer;
import io.opentracing.contrib.java.spring.jaeger.starter.customizers.HigherBitTracerBuilderCustomizer;
import io.opentracing.contrib.java.spring.jaeger.starter.customizers.TraceContextCodecTracerBuilderCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Configuration
@ConditionalOnClass(io.jaegertracing.internal.JaegerTracer.class)
@ConditionalOnMissingBean(io.opentracing.Tracer.class)
@ConditionalOnProperty(value = "opentracing.jaeger.enabled", havingValue = "true", matchIfMissing = true)
@AutoConfigureBefore(io.opentracing.contrib.spring.tracer.configuration.TracerAutoConfiguration.class)
@EnableConfigurationProperties(JaegerConfigurationProperties.class)
public class JaegerCustomConfiguration {

    @Autowired(required = false)
    private final List<TracerBuilderCustomizer> tracerCustomizers = Collections.emptyList();

    @Bean
    public io.opentracing.Tracer tracer(Sampler sampler, Reporter reporter, Metrics metrics, JaegerConfigurationProperties properties) {
        //Customização do nome da aplicação
        final JaegerTracer.Builder builder = new JaegerTracer.Builder("demo-jaeger-lib-webflux-test").withReporter(reporter).withSampler(sampler).withTags(properties.determineTags()).withMetrics(metrics);

        tracerCustomizers.forEach(c -> c.customize(builder));

        return builder.build();
    }

    @ConditionalOnMissingBean
    @Bean
    public Reporter reporter(JaegerConfigurationProperties properties, Metrics metrics, @Autowired(required = false) ReporterAppender reporterAppender) {

        List<Reporter> reporters = new LinkedList<>();
        JaegerConfigurationProperties.RemoteReporter remoteReporter = properties.getRemoteReporter();

        JaegerConfigurationProperties.HttpSender httpSender = properties.getHttpSender();
        if (StringUtils.hasLength(httpSender.getUrl())) {
            reporters.add(getHttpReporter(metrics, remoteReporter, httpSender));
        } else {
            reporters.add(getUdpReporter(metrics, remoteReporter, properties.getUdpSender()));
        }

        if (properties.isLogSpans()) {
            reporters.add(new LoggingReporter());
        }

        if (reporterAppender != null) {
            reporterAppender.append(reporters);
        }

        return new CompositeReporter(reporters.toArray(new Reporter[0]));
    }

    private Reporter getUdpReporter(Metrics metrics, JaegerConfigurationProperties.RemoteReporter remoteReporter, JaegerConfigurationProperties.UdpSender udpSenderProperties) {
        io.jaegertracing.thrift.internal.senders.UdpSender udpSender = new io.jaegertracing.thrift.internal.senders.UdpSender(udpSenderProperties.getHost(), udpSenderProperties.getPort(), udpSenderProperties.getMaxPacketSize());

        return createReporter(metrics, remoteReporter, udpSender);
    }

    private Reporter getHttpReporter(Metrics metrics, JaegerConfigurationProperties.RemoteReporter remoteReporter, JaegerConfigurationProperties.HttpSender httpSenderProperties) {
        io.jaegertracing.thrift.internal.senders.HttpSender.Builder builder = new io.jaegertracing.thrift.internal.senders.HttpSender.Builder(httpSenderProperties.getUrl());
        if (httpSenderProperties.getMaxPayload() != null) {
            builder = builder.withMaxPacketSize(httpSenderProperties.getMaxPayload());
        }
        if (StringUtils.hasLength(httpSenderProperties.getUsername()) && StringUtils.hasLength(httpSenderProperties.getPassword())) {
            builder.withAuth(httpSenderProperties.getUsername(), httpSenderProperties.getPassword());
        } else if (StringUtils.hasLength(httpSenderProperties.getAuthToken())) {
            builder.withAuth(httpSenderProperties.getAuthToken());
        }

        return createReporter(metrics, remoteReporter, builder.build());
    }

    private Reporter createReporter(Metrics metrics, JaegerConfigurationProperties.RemoteReporter remoteReporter, Sender udpSender) {
        io.jaegertracing.internal.reporters.RemoteReporter.Builder builder = new io.jaegertracing.internal.reporters.RemoteReporter.Builder().withSender(udpSender).withMetrics(metrics);

        if (remoteReporter.getFlushInterval() != null) {
            builder.withFlushInterval(remoteReporter.getFlushInterval());
        }
        if (remoteReporter.getMaxQueueSize() != null) {
            builder.withMaxQueueSize(remoteReporter.getMaxQueueSize());
        }

        return builder.build();
    }

    @ConditionalOnMissingBean
    @Bean
    public Metrics metrics(MetricsFactory metricsFactory) {
        return new Metrics(metricsFactory);
    }

    @ConditionalOnMissingBean
    @Bean
    public MetricsFactory metricsFactory() {
        return new NoopMetricsFactory();
    }

    /**
     * Decide on what Sampler to use based on the various configuration options in
     * JaegerConfigurationProperties Fallback to ConstSampler(true) when no Sampler is configured
     */
    @ConditionalOnMissingBean
    @Bean
    public Sampler sampler(JaegerConfigurationProperties properties, Metrics metrics) {
        if (properties.getConstSampler().getDecision() != null) {
            return new ConstSampler(properties.getConstSampler().getDecision());
        }

        if (properties.getProbabilisticSampler().getSamplingRate() != null) {
            return new ProbabilisticSampler(properties.getProbabilisticSampler().getSamplingRate());
        }

        if (properties.getRateLimitingSampler().getMaxTracesPerSecond() != null) {
            return new RateLimitingSampler(properties.getRateLimitingSampler().getMaxTracesPerSecond());
        }

        if (StringUtils.hasLength(properties.getRemoteControlledSampler().getHostPort())) {
            JaegerConfigurationProperties.RemoteControlledSampler samplerProperties = properties.getRemoteControlledSampler();

            String hostPort = samplerProperties.getHostPort();

            if (samplerProperties.getHost() != null && !samplerProperties.getHost().isEmpty()) {
                hostPort = samplerProperties.getHost() + ":" + samplerProperties.getPort();
            }

            return new RemoteControlledSampler.Builder(properties.getServiceName()).withSamplingManager(new HttpSamplingManager(hostPort)).withInitialSampler(new ProbabilisticSampler(samplerProperties.getSamplingRate())).withMetrics(metrics).build();
        }

        //fallback to sampling every trace
        return new ConstSampler(true);
    }

    @Configuration
    @ConditionalOnProperty(value = "opentracing.jaeger.enable-b3-propagation")
    public static class B3CodecConfiguration {

        @Bean
        public TracerBuilderCustomizer b3CodecJaegerTracerCustomizer() {
            return new B3CodecTracerBuilderCustomizer();
        }
    }

    @Configuration
    @ConditionalOnProperty(value = "opentracing.jaeger.enable-w3c-propagation")
    public static class TraceContextCodecConfiguration {

        @Bean
        public TracerBuilderCustomizer traceContextCodecJaegerTracerCustomizer() {
            return new TraceContextCodecTracerBuilderCustomizer();
        }
    }

    @Configuration
    @ConditionalOnProperty(value = "opentracing.jaeger.enable-128-bit-traces")
    public static class HigherBitTraceConfiguration {
        @Bean
        public TracerBuilderCustomizer higherBitJaegerTracerCustomizer() {
            return new HigherBitTracerBuilderCustomizer();
        }
    }

    @Configuration
    @ConditionalOnProperty(value = "opentracing.jaeger.expand-exception-logs")
    public static class ExpandExceptionLogsConfiguration {

        @Bean
        public TracerBuilderCustomizer expandExceptionLogsJaegerTracerCustomizer() {
            return new ExpandExceptionLogsTracerBuilderCustomizer();
        }
    }
}
