export interface AuthCodeConfig {
  clientId: string;
  clientSecret: string;
  scopes: string;
  authorizationEndpoint: string;
  tokenEndpoint: string;
  userInfoEndpoint: string;
  jwkSetUri: string;
  // 可选的身份映射配置
  identityMapping?: {
    userIdField?: string;
    userNameField?: string;
    emailField?: string;
  };
}

export interface OidcConfig {
  provider: string;
  name: string;
  logoUrl: string;
  enabled: boolean;
  grantType: 'AUTHORIZATION_CODE';
  authCodeConfig: AuthCodeConfig;
}

// 第三方认证相关类型定义
export enum AuthenticationType {
  OIDC = 'OIDC',
  OAUTH2 = 'OAUTH2'
}

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

// 独立的OAuth2配置（用于第三方认证配置中）
export interface OAuth2AuthConfig {
  grantType: GrantType;
  jwtBearerConfig?: JwtBearerConfig;
  identityMapping: IdentityMapping;
}

// 旧的OAuth2配置（向后兼容）
export interface OAuth2Config {
  provider: string;
  name: string;
  grantType: GrantType;
  jwtBearerConfig?: JwtBearerConfig;
  identityMapping: IdentityMapping;
}

// 统一的第三方认证配置
export interface ThirdPartyAuthConfig {
  provider: string;
  name: string;
  type: AuthenticationType;
  enabled: boolean;
  
  // OIDC配置（当type为OIDC时使用）
  oidcConfig?: {
    grantType: 'AUTHORIZATION_CODE';
    authCodeConfig: AuthCodeConfig;
  };
  
  // OAuth2配置（当type为OAUTH2时使用）
  oauth2Config?: OAuth2AuthConfig;
}

export interface PortalSettingConfig {
  builtinAuthEnabled: boolean;
  oidcAuthEnabled: boolean;
  autoApproveDevelopers: boolean;
  autoApproveSubscriptions: boolean;
  frontendRedirectUrl: string;
  
  // 向后兼容的旧字段（逐步废弃）
  oidcConfigs?: OidcConfig[];
  oauth2Configs?: OAuth2Config[];
  
  // 新的统一第三方认证配置
  thirdPartyAuthConfigs: ThirdPartyAuthConfig[];
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