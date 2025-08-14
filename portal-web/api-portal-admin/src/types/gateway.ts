export interface Gateway {
  gatewayId: string
  gatewayName: string
  gatewayType: 'APIG_API' | 'HIGRESS' | 'APIG_AI'
  createAt: string
}

export interface ApigConfig {
  region: string
  accessKey: string
  secretKey: string
}

export interface HigressConfig {
  host: string
  port: number
  accessToken: string
  jwtPolicy: string
}

export type GatewayType = 'APIG_API' | 'APIG_AI' | 'HIGRESS'
