{{- define "product-api-portal.serviceAccountName" -}}
{{- default "default" .Values.serviceAccount.name }}
{{- end }}
