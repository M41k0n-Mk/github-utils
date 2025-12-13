package me.m41k0n.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.mail.to:}")
    private String mailTo;

    @Value("${app.mail.from:github-utils@localhost}")
    private String mailFrom;

    public EmailService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSenderProvider = mailSenderProvider;
    }

    public void sendUnfollowSummary(int totalTargets, int executed, int skippedExcluded, boolean dryRun) {
        if (!mailEnabled) {
            log.info("[MAIL] Envio de e-mail desabilitado (app.mail.enabled=false). Resumo: total={}, executados={}, ignoradosPorExclusao={}, dryRun={}",
                    totalTargets, executed, skippedExcluded, dryRun);
            return;
        }
        if (mailTo == null || mailTo.isBlank()) {
            log.warn("[MAIL] app.mail.to não configurado. Pulando envio de e-mail de resumo.");
            return;
        }
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.info("[MAIL] JavaMailSender não está configurado. Pulando envio de e-mail.");
            return;
        }
        try {
            String subject = dryRun ? "[GitHub Utils] Preview DRY-RUN - Resumo de Unfollow" : "[GitHub Utils] Resumo de Unfollow";
            StringBuilder body = new StringBuilder();
            body.append("Resumo da execução de Unfollow\n")
                .append("Dry-run: ").append(dryRun).append('\n')
                .append("Total candidatos: ").append(totalTargets).append('\n')
                .append("Executados: ").append(executed).append('\n')
                .append("Ignorados por exclusão: ").append(skippedExcluded).append('\n');

            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(mailFrom);
            msg.setTo(mailTo);
            msg.setSubject(subject);
            msg.setText(body.toString());
            mailSender.send(msg);
            log.info("[MAIL] Resumo de unfollow enviado para {}", mailTo);
        } catch (Exception e) {
            log.warn("[MAIL] Falha ao enviar e-mail de resumo: {}", e.getMessage());
        }
    }
}
