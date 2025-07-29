export interface OidcConfig {
  id: string;
  provider: string;
  name: string;
  logoUrl: string;
  clientId: string;
  clientSecret: string;
  scopes: string;
  authorizationEndpoint: string;
  tokenEndpoint: string;
  userInfoEndpoint: string;
  jwkSetUri: string;
  redirectUri: string;
  enabled: boolean;
}

export interface PortalSettingConfig {
  builtinAuthEnabled: boolean;
  oidcAuthEnabled: boolean;
  autoApproveDevelopers: boolean;
  autoApproveSubscriptions: boolean;
  frontendRedirectUrl: string;
  oidcConfigs: OidcConfig[];
}

export interface PortalUiConfig {
  logo: string | null;
  icon: string | null;
}

export interface PortalDomainConfig {
  domain: string;
  type: string;
  protocol: string;
}

export interface Portal {
  portalId: string;
  name: string;
  title: string;
  description: string;
  adminId: string;
  portalSettingConfig: PortalSettingConfig;
  portalUiConfig: PortalUiConfig;
  portalDomainConfig: PortalDomainConfig[];
} 