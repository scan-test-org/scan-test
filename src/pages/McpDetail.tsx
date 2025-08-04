import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import api from "../lib/api";
import { Layout } from "../components/Layout";
import {
  Card,
  Typography,
  Tag,
  Space,
  Descriptions,
  Spin,
  Alert,
  Collapse,
  Button,
  message,
  Tabs,
  Row,
  Col,
} from "antd";
import MonacoEditor from "react-monaco-editor";
import ReactMarkdown from "react-markdown";
import { ProductType, ProductCategory } from "../types";
import type {
  Product,
  McpServerConfig,
  McpServerProduct,
  McpConfig,
} from "../types";
import * as yaml from "js-yaml";

const { Title, Paragraph } = Typography;

// 来源类型映射
const FromTypeMap: Record<string, string> = {
  HTTP: "HTTP转MCP",
  MCP: "MCP直接代理",
  OPEN_API: "OpenAPI转MCP",
  DIRECT_ROUTE: "直接路由",
  DATABASE: "数据库",
};

// 来源映射
const SourceMap: Record<string, string> = {
  APIG_AI: "AI网关",
  HIGRESS: "Higress",
  NACOS: "Nacos",
};

// 状态映射
const StatusMap: Record<string, { text: string; color: string }> = {
  PENDING: { text: "待发布", color: "orange" },
  READY: { text: "就绪", color: "blue" },
  PUBLISHED: { text: "已发布", color: "green" },
};

function McpDetail() {
  const { mcpName } = useParams();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [data, setData] = useState<Product | null>(null);
  const [mcpConfig, setMcpConfig] = useState<McpServerConfig | null>(null);
  const [nacosMcpConfig, setNacosMcpConfig] = useState<McpConfig | null>(null);
  const [parsedTools, setParsedTools] = useState<
    Array<{
      name: string;
      description: string;
      args?: Array<{
        name: string;
        description: string;
        type: string;
        required: boolean;
        position: string;
        default?: string;
        enum?: string[];
      }>;
    }>
  >([]);
  const [httpJson, setHttpJson] = useState("");
  const [sseJson, setSseJson] = useState("");
  const [localJson, setLocalJson] = useState("");

  // 解析YAML配置的函数
  const parseYamlConfig = (
    yamlString: string
  ): {
    tools?: Array<{
      name: string;
      description: string;
      args?: Array<{
        name: string;
        description: string;
        type: string;
        required: boolean;
        position: string;
        default?: string;
        enum?: string[];
      }>;
    }>;
  } | null => {
    try {
      const parsed = yaml.load(yamlString) as {
        tools?: Array<{
          name: string;
          description: string;
          args?: Array<{
            name: string;
            description: string;
            type: string;
            required: boolean;
            position: string;
            default?: string;
            enum?: string[];
          }>;
        }>;
      };
      return parsed;
    } catch (error) {
      console.warn("解析YAML配置失败:", error);
      return null;
    }
  };

  // 生成连接配置的函数
  const generateConnectionConfig = (
    domains: Array<{ domain: string; protocol: string }>,
    path: string,
    serverName: string,
    localConfig?: unknown
  ) => {
    if (domains && domains.length > 0) {
      const domain = domains[0];
      const baseUrl = `${domain.protocol}://${domain.domain}`;
      const endpoint = `${baseUrl}${path}`;

      // HTTP配置
      const httpConfig = `{
  "mcpServers": {
    "${serverName}": {
      "url": "${endpoint}"
    }
  }
}`;

      // SSE配置
      const sseConfig = `{
  "mcpServers": {
    "${serverName}": {
      "url": "${endpoint}/sse"
    }
  }
}`;

      // Local配置
      const localConfigJson = localConfig
        ? `{
  "mcpServers": {
    "${serverName}": ${JSON.stringify(localConfig, null, 2)}
  }
}`
        : `{
  "mcpServers": {
    "${serverName}": {
      "command": "your-local-command",
      "args": ["arg1", "arg2"]
    }
  }
}`;

      setHttpJson(httpConfig);
      setSseJson(sseConfig);
      setLocalJson(localConfigJson);
    }
  };

  useEffect(() => {
    console.log("useEffect 触发，mcpName:", mcpName);
    if (!mcpName) {
      console.log("mcpName 为空，返回");
      return;
    }
    setLoading(true);
    setError("");
    api
      .get(`/products/${mcpName}`)
      .then((response: any) => {
        if (response.code === "SUCCESS" && response.data) {
          setData(response.data);

          // 处理MCP配置 - 支持新旧两种格式
          if (response.data.type === ProductType.MCP_SERVER) {
            const mcpProduct = response.data as McpServerProduct;

            // 优先处理新的nacos格式
            if (mcpProduct.mcpConfig) {
              setNacosMcpConfig(mcpProduct.mcpConfig);

              // 解析tools配置
              if (mcpProduct.mcpConfig.tools) {
                const parsedConfig = parseYamlConfig(
                  mcpProduct.mcpConfig.tools
                );
                if (parsedConfig && parsedConfig.tools) {
                  setParsedTools(parsedConfig.tools);
                }
              }

              // 生成连接配置
              generateConnectionConfig(
                mcpProduct.mcpConfig.mcpServerConfig.domains,
                mcpProduct.mcpConfig.mcpServerConfig.path,
                mcpProduct.mcpConfig.mcpServerName,
                mcpProduct.mcpConfig.mcpServerConfig.localConfig
              );
            }
            // 兼容旧格式
            else if (mcpProduct.mcpSpec) {
              setMcpConfig(mcpProduct.mcpSpec);

              // 解析YAML配置中的工具信息
              if (mcpProduct.mcpSpec.mcpServerConfig) {
                const parsedConfig = parseYamlConfig(
                  mcpProduct.mcpSpec.mcpServerConfig
                );
                if (parsedConfig && parsedConfig.tools) {
                  setParsedTools(parsedConfig.tools);
                }
              }

              // 生成连接配置
              if (
                mcpProduct.mcpSpec.domains &&
                mcpProduct.mcpSpec.domains.length > 0
              ) {
                const domain = mcpProduct.mcpSpec.domains[0];
                const baseUrl = `${domain.protocol}://${domain.domain}`;
                const httpConfig = `{
  "mcpServers": {
    "${mcpName}": {
      "url": "${baseUrl}/mcp-servers/${mcpName}"
    }
  }
}`;
                const sseConfig = `{
  "mcpServers": {
    "${mcpName}": {
      "url": "${baseUrl}/mcp-servers/${mcpName}/sse"
    }
  }
}`;
                setHttpJson(httpConfig);
                setSseJson(sseConfig);
                setLocalJson(`{
  "mcpServers": {
    "${mcpName}": {
      "command": "your-local-command",
      "args": ["arg1", "arg2"]
    }
  }
}`);
              }
            }
          }
        } else {
          console.log("API 响应失败:", response);
          setError(response.message || "数据加载失败");
        }
      })
      .catch((error) => {
        console.error("API请求失败:", error);
        setError("加载失败，请稍后重试");
      })
      .finally(() => {
        console.log("请求完成，设置 loading 为 false");
        setLoading(false);
      });
  }, [mcpName]);

  const handleCopy = async (text: string) => {
    try {
      if (navigator.clipboard && window.isSecureContext) {
        await navigator.clipboard.writeText(text);
      } else {
        // 非安全上下文降级处理
        const textarea = document.createElement("textarea");
        textarea.value = text;
        textarea.style.position = "fixed";
        document.body.appendChild(textarea);
        textarea.focus();
        textarea.select();
        document.execCommand("copy");
        document.body.removeChild(textarea);
      }
      message.success("已复制到剪贴板");
    } catch {
      message.error("复制失败，请手动复制");
    }
  };

  if (loading) {
    return (
      <Layout>
        <div className="flex justify-center items-center min-h-[300px]">
          <Spin size="large" />
        </div>
      </Layout>
    );
  }
  if (error) {
    return (
      <Layout>
        <Alert message={error} type="error" showIcon className="my-8" />
      </Layout>
    );
  }
  if (!data) {
    return (
      <Layout>
        <Alert
          message="未找到相关数据"
          type="warning"
          showIcon
          className="my-8"
        />
      </Layout>
    );
  }

  const { name, description, status, category, enableConsumerAuth } = data;
  const currentMcpConfig = nacosMcpConfig || mcpConfig;
  const hasLocalConfig = nacosMcpConfig?.mcpServerConfig.localConfig || false;

  const getStatusInfo = (status: string) => {
    return StatusMap[status] || { text: status, color: "default" };
  };

  const getCategoryText = (category: ProductCategory) => {
    switch (category) {
      case ProductCategory.OFFICIAL:
        return "官方";
      case ProductCategory.COMMUNITY:
        return "社区";
      case ProductCategory.CUSTOM:
        return "自定义";
      default:
        return category;
    }
  };

  return (
    <Layout>
      {/* 头部信息区域 */}
      <div className="mb-8">
        <div className="flex items-start gap-4 mb-4">
          <img
            src={data.icon || "/MCP.png"}
            alt="icon"
            className="w-16 h-16 rounded-lg object-cover border"
            onError={(e) => {
              const target = e.target as HTMLImageElement;
              target.src = "/default-icon.png";
            }}
          />
                      <div className="flex-1">
              <Title level={1} className="mb-2">
                {name}
              </Title>
              <Space className="mb-3">
                <Tag color={getStatusInfo(status).color}>
                  {getStatusInfo(status).text}
                </Tag>
                <Tag color="blue">{getCategoryText(category)}</Tag>
                <Tag color="purple">Model Context Protocol</Tag>
                <Tag color={typeof (data as McpServerProduct).enabled !== "undefined" && (data as McpServerProduct).enabled ? "green" : "red"}>
                  {typeof (data as McpServerProduct).enabled !== "undefined"
                    ? (data as McpServerProduct).enabled
                      ? "已启用"
                      : "未启用"
                    : "未知"}
                </Tag>
                <Tag color={enableConsumerAuth ? "green" : "orange"}>
                  消费者认证: {enableConsumerAuth ? "启用" : "禁用"}
                </Tag>
              </Space>
            </div>
          </div>
          <Paragraph className="text-gray-600 mb-3">{description}</Paragraph>
      </div>

      {/* 主要内容区域 - 左右布局 */}
      <Row gutter={24}>
        {/* 左侧内容 */}
        <Col span={16}>
          <Card className="mb-6">
            <Tabs
              defaultActiveKey="overview"
              items={[
                {
                  key: "overview",
                  label: "Overview",
                  children: data.document ? (
                    <div className="prose max-w-none">
                      <ReactMarkdown>{data.document}</ReactMarkdown>
                    </div>
                  ) : (
                    <div className="text-gray-500 text-center py-8">
                      暂无文档内容
                    </div>
                  ),
                },
                {
                  key: "config",
                  label: "MCP Config",
                  children: currentMcpConfig ? (
                    <div>
                      <Descriptions column={1} bordered size="small">
                        <Descriptions.Item label="MCP Server Name">
                          {nacosMcpConfig?.mcpServerName ||
                            mcpConfig?.mcpServerName ||
                            "无"}
                        </Descriptions.Item>
                        <Descriptions.Item label="来源类型">
                          {nacosMcpConfig?.meta.fromType
                            ? FromTypeMap[nacosMcpConfig.meta.fromType] ||
                              nacosMcpConfig.meta.fromType
                            : mcpConfig?.fromType || "无"}
                        </Descriptions.Item>
                        <Descriptions.Item label="来源">
                          {nacosMcpConfig?.meta.source
                            ? SourceMap[nacosMcpConfig.meta.source] ||
                              nacosMcpConfig.meta.source
                            : mcpConfig?.fromGatewayType || "无"}
                        </Descriptions.Item>
                        <Descriptions.Item label="路径">
                          {nacosMcpConfig?.mcpServerConfig.path || "无"}
                        </Descriptions.Item>
                        <Descriptions.Item label="运行模式">
                          {hasLocalConfig ? "Local Mode" : "SSE/HTTP Mode"}
                        </Descriptions.Item>
                        <Descriptions.Item label="域名配置">
                          {(nacosMcpConfig?.mcpServerConfig.domains ||
                            mcpConfig?.domains) &&
                          (nacosMcpConfig?.mcpServerConfig.domains ||
                            mcpConfig?.domains)!.length > 0
                            ? (nacosMcpConfig?.mcpServerConfig.domains ||
                                mcpConfig?.domains)!.map((d, i) => (
                                <div key={i} className="mb-2">
                                  <div>
                                    <strong>域名:</strong> {d.domain}
                                  </div>
                                  <div>
                                    <strong>协议:</strong> {d.protocol}
                                  </div>
                                </div>
                              ))
                            : "无"}
                        </Descriptions.Item>
                      </Descriptions>
                    </div>
                  ) : (
                    <div className="text-gray-500 text-center py-8">
                      暂无MCP配置信息
                    </div>
                  ),
                },
                                  {
                    key: "tools",
                    label: `Tools (${parsedTools.length})`,
                    children:
                      parsedTools.length > 0 ? (
                        <Collapse accordion>
                          {parsedTools.map((tool, idx) => (
                          <Collapse.Panel header={tool.name} key={idx}>
                            <div className="mb-2 text-gray-600">
                              {tool.description}
                            </div>
                            <div className="mb-2 font-bold">输入参数：</div>
                            <div className="space-y-2">
                              {tool.args?.map(
                                (
                                  arg: {
                                    name: string;
                                    description: string;
                                    type: string;
                                    required: boolean;
                                    position: string;
                                    default?: string;
                                    enum?: string[];
                                  },
                                  argIdx: number
                                ) => (
                                  <div
                                    key={argIdx}
                                    className="flex flex-col mb-2"
                                  >
                                    <div className="flex items-center mb-1">
                                      <span className="font-medium mr-2">
                                        {arg.name}
                                      </span>
                                      <span className="text-xs text-gray-500 mr-2">
                                        ({arg.type})
                                      </span>
                                      {arg.required && (
                                        <span className="text-red-500 text-xs">
                                          *
                                        </span>
                                      )}
                                    </div>
                                    {arg.description && (
                                      <div className="text-xs text-gray-500 mb-1">
                                        {arg.description}
                                      </div>
                                    )}
                                    <input
                                      disabled
                                      className="border rounded px-2 py-1 text-sm bg-gray-100 w-full max-w-md"
                                      placeholder={arg.default ?? ""}
                                    />
                                  </div>
                                )
                              )}
                              {(!tool.args || tool.args.length === 0) && (
                                <span className="text-gray-400">无参数</span>
                              )}
                            </div>
                          </Collapse.Panel>
                        ))}
                      </Collapse>
                    ) : (
                      <div className="text-gray-500 text-center py-8">
                        暂无工具配置
                      </div>
                    ),
                },
              ]}
            />
          </Card>
        </Col>

        {/* 右侧配置信息 */}
        <Col span={8}>

          {/* 连接配置 */}
          {((mcpConfig && mcpConfig.domains && mcpConfig.domains.length > 0) ||
            (nacosMcpConfig &&
              nacosMcpConfig.mcpServerConfig.domains &&
              nacosMcpConfig.mcpServerConfig.domains.length > 0)) && (
            <Card title="连接配置" className="mb-6">
              <Tabs
                defaultActiveKey={hasLocalConfig ? "local" : "sse"}
                items={[
                  ...(hasLocalConfig
                    ? [
                        {
                          key: "local",
                          label: "Local Config",
                          children: (
                            <div className="relative">
                              <div className="absolute top-2 right-2">
                                <Button
                                  size="small"
                                  onClick={() => handleCopy(localJson)}
                                >
                                  复制
                                </Button>
                              </div>
                              <div
                                style={{
                                  width: "100%",
                                  overflowX: "auto",
                                  height: "200px",
                                }}
                              >
                                <MonacoEditor
                                  language="json"
                                  theme="vs-dark"
                                  value={localJson}
                                  options={{
                                    readOnly: true,
                                    minimap: { enabled: false },
                                    scrollBeyondLastLine: false,
                                    scrollbar: {
                                      vertical: "visible",
                                      horizontal: "visible",
                                    },
                                    wordWrap: "off",
                                    lineNumbers: "on",
                                    automaticLayout: true,
                                    fontSize: 12,
                                    copyWithSyntaxHighlighting: true,
                                    contextmenu: true,
                                  }}
                                  height="200"
                                />
                              </div>
                            </div>
                          ),
                        },
                      ]
                    : []),
                  {
                    key: "http",
                    label: "HTTP Config",
                    children: (
                      <div className="relative">
                        <div className="absolute top-2 right-2">
                          <Button
                            size="small"
                            onClick={() => handleCopy(httpJson)}
                          >
                            复制
                          </Button>
                        </div>
                        <div
                          style={{
                            width: "100%",
                            overflowX: "auto",
                            height: "200px",
                          }}
                        >
                          <MonacoEditor
                            language="json"
                            theme="vs-dark"
                            value={httpJson}
                            options={{
                              readOnly: true,
                              minimap: { enabled: false },
                              scrollBeyondLastLine: false,
                              scrollbar: {
                                vertical: "visible",
                                horizontal: "visible",
                              },
                              wordWrap: "off",
                              lineNumbers: "on",
                              automaticLayout: true,
                              fontSize: 12,
                              copyWithSyntaxHighlighting: true,
                              contextmenu: true,
                            }}
                            height="200"
                          />
                        </div>
                      </div>
                    ),
                  },
                  {
                    key: "sse",
                    label: "SSE Config",
                    children: (
                      <div className="relative">
                        <div className="absolute top-2 right-2">
                          <Button
                            size="small"
                            onClick={() => handleCopy(sseJson)}
                          >
                            复制
                          </Button>
                        </div>
                        <div
                          style={{
                            width: "100%",
                            overflowX: "auto",
                            height: "200px",
                          }}
                        >
                          <MonacoEditor
                            language="json"
                            theme="vs-dark"
                            value={sseJson}
                            options={{
                              readOnly: true,
                              minimap: { enabled: false },
                              scrollBeyondLastLine: false,
                              scrollbar: {
                                vertical: "visible",
                                horizontal: "visible",
                              },
                              wordWrap: "off",
                              lineNumbers: "on",
                              automaticLayout: true,
                              fontSize: 12,
                              copyWithSyntaxHighlighting: true,
                              contextmenu: true,
                            }}
                            height="200"
                          />
                        </div>
                      </div>
                    ),
                  },
                ]}
              />
            </Card>
          )}
        </Col>
      </Row>
    </Layout>
  );
}

export default McpDetail;
