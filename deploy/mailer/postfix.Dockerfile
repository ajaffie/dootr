FROM catatnight/postfix:latest
ARG creds
ENV smtp_user=$creds
ENV maildomain=dootr.ajaffie.dev
COPY mail.private /etc/opendkim/domainkeys/.private
