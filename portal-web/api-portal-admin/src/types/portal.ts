export interface AuthCodeConfig {
  clientId: string;
  clientSecret: string;
  scopes: string;
  authorizationEndpoint: string;
  tokenEndpoint: string;
  userInfoEndpoint: string;
  jwkSetUri: string;
}

export interface OidcConfig {
  provider: string;
  name: string;
  logoUrl: string;
  enabled: boolean;
  grantType: 'AUTHORIZATION_CODE';
  authCodeConfig: AuthCodeConfig;
}

// OAuth2 相关类型定义
export enum GrantType {
  AUTHORIZATION_CODE = 'AUTHORIZATION_CODE',
  JWT_BEARER = 'JWT_BEARER'
}

export enum PublicKeyFormat {
  PEM = 'PEM',
  JWK = 'JWK'
}

export interface PublicKeyConfig {
  kid: string;
  format: PublicKeyFormat;
  algorithm: string;
  value: string;
}

export interface JwtBearerConfig {
  publicKeys: PublicKeyConfig[];
}

export interface IdentityMapping {
  userIdField: string;
  userNameField: string;
  customFields?: { [key: string]: string };
}

export interface OAuth2Config {
  provider: string;
  name: string;
  grantType: GrantType;
  jwtBearerConfig?: JwtBearerConfig;
  identityMapping: IdentityMapping;
}

export interface PortalSettingConfig {
  builtinAuthEnabled: boolean;
  oidcAuthEnabled: boolean;
  autoApproveDevelopers: boolean;
  autoApproveSubscriptions: boolean;
  frontendRedirectUrl: string;
  oidcConfigs: OidcConfig[];
  oauth2Configs: OAuth2Config[];
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

export interface Developer {
  portalId: string;
  developerId: string;
  username: string;
  status: string;
  avatarUrl?: string;
  createAt: string;
}