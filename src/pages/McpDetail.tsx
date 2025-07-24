import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import api from '../lib/api';
import { Layout } from '../components/Layout';
import { Card, Typography, Tag, Space, Table, Badge, Descriptions, Spin, Alert, Collapse } from 'antd';
import ReactMarkdown from 'react-markdown';
import MonacoEditor from 'react-monaco-editor';
import { Button, message } from 'antd';

const { Title, Paragraph } = Typography;

const protocolColor: Record<string, string> = {
  stdio: 'blue',
  http: 'green',
  sse: 'orange',
};

// 定义类型
interface Tool {
  name: string;
  description: string;
  inputSchema: Record<string, any>;
}
interface ToolSpec {
  tools: Tool[];
}
interface VersionDetail {
  version: string;
}
interface LocalServerConfig {
  [key: string]: any;
}
interface VersionItem {
  version: string;
  release_date: string;
  is_latest: boolean;
}
interface McpDetailData {
  name: string;
  description: string;
  protocol: string;
  frontProtocol: string;
  version: string;
  versionDetail?: VersionDetail;
  enabled: boolean;
  capabilities?: string[];
  toolSpec?: ToolSpec;
  localServerConfig?: LocalServerConfig;
  allVersions?: VersionItem[];
  overview?: string;
}

// 工具参数渲染组件
function ToolInputSchemaView({ inputSchema }: { inputSchema: Record<string, any> }) {
  if (!inputSchema || typeof inputSchema !== 'object') return <span className="text-gray-400">无参数</span>;
  const properties = inputSchema.properties || {};
  const required = inputSchema.required || [];
  return (
    <div className="space-y-2">
      {Object.entries(properties).map(([key, value]: [string, any]) => (
        <div key={key} className="flex flex-col mb-2">
          <div className="flex items-center mb-1">
            <span className="font-medium mr-2">{key}</span>
            <span className="text-xs text-gray-500 mr-2">({value.type || '未知类型'})</span>
            {required.includes(key) && <span className="text-red-500 text-xs">*</span>}
          </div>
          {value.description && <div className="text-xs text-gray-500 mb-1">{value.description}</div>}
          <input disabled className="border rounded px-2 py-1 text-sm bg-gray-100 w-full max-w-md" placeholder={value.example || ''} />
        </div>
      ))}
      {Object.keys(properties).length === 0 && <span className="text-gray-400">无参数</span>}
    </div>
  );
}

function McpDetail() {
  const { mcpName } = useParams();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [data, setData] = useState<McpDetailData | null>(null);

  useEffect(() => {
    if (!mcpName) return;
    setLoading(true);
    setError('');
    api.get(`/mcpmarket/${mcpName}`)
      .then((res: { data: McpDetailData }) => {
        setData(res.data || res);
      })
      .catch(() => {
        setError('加载失败，请稍后重试');
      })
      .finally(() => setLoading(false));
  }, [mcpName]);

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
    return null;
  }

  const { name, description, protocol, frontProtocol, version, versionDetail, enabled, capabilities, toolSpec, localServerConfig, allVersions, overview } = data;

  return (
    <Layout>
      <div className="mb-8">
        <Title level={1} className="mb-2">{name}</Title>
        <Space className="mb-4">
          <Badge status={enabled ? 'success' : 'default'} text={enabled ? '已启用' : '未启用'} />
          <Tag color={protocolColor[protocol] || 'default'}>{protocol}</Tag>
          <Tag color="blue">{version}</Tag>
          {capabilities && capabilities.map((cap) => (
            <Tag key={cap} color="purple">{cap}</Tag>
          ))}
        </Space>
        <Paragraph className="text-gray-600">{description}</Paragraph>
      </div>

      <Card title="基本信息" className="mb-6">
        <Descriptions column={1} bordered size="small">
          <Descriptions.Item label="协议">{protocol}</Descriptions.Item>
          <Descriptions.Item label="前端协议">{frontProtocol}</Descriptions.Item>
          <Descriptions.Item label="当前版本">{versionDetail?.version || version}</Descriptions.Item>
          <Descriptions.Item label="能力">{capabilities && capabilities.join(', ')}</Descriptions.Item>
        </Descriptions>
      </Card>、

      {/* 概览区域 */}
      <Card title="概览" className="mb-6">
        <Collapse defaultActiveKey={[]}>
          <Collapse.Panel header="概览" key="overview">
            {overview ? (
              <ReactMarkdown>{overview}</ReactMarkdown>
            ) : (
              <span className="text-gray-400">暂无概览</span>
            )}
          </Collapse.Panel>
        </Collapse>
      </Card>

      {/* 工具列表折叠展示 */}
      {toolSpec && toolSpec.tools && toolSpec.tools.length > 0 && (
        <Card title="工具列表" className="mb-6">
          <Collapse accordion>
            {toolSpec.tools.map((tool, idx) => (
              <Collapse.Panel header={tool.name} key={idx}>
                <div className="mb-2 text-gray-600">{tool.description}</div>
                <div className="mb-2 font-bold">输入参数：</div>
                <ToolInputSchemaView inputSchema={tool.inputSchema} />
                {/* 这里可扩展参数输入表单等 */}
              </Collapse.Panel>
            ))}
          </Collapse>
        </Card>
      )}

      {localServerConfig && (
        <Card title="MCP Server 配置" className="mb-6">
          <div className="mb-2 flex justify-end">
            <Button size="small" onClick={() => {
              navigator.clipboard.writeText(JSON.stringify(localServerConfig, null, 2));
              message.success('已复制到剪贴板');
            }}>复制</Button>
          </div>
          <div style={{ width: '100%', overflowX: 'auto', height: '300px' }}>
            <MonacoEditor
              language="json"
              theme="vs"
              value={JSON.stringify(localServerConfig, null, 2)}
              options={{
                readOnly: true,
                minimap: { enabled: false },
                scrollBeyondLastLine: false,
                scrollbar: { vertical: 'hidden', horizontal: 'hidden' },
                wordWrap: 'on',
                lineNumbers: 'on',
                automaticLayout: true,
              }}
              height="300"
            />
          </div>
        </Card>
      )}

      {allVersions && allVersions.length > 0 && (
        <Card title="历史版本" className="mb-6">
          <Table
            columns={[
              { title: '版本', dataIndex: 'version', key: 'version' },
              { title: '发布日期', dataIndex: 'release_date', key: 'release_date', render: (d: string) => d ? d.split('T')[0] : '-' },
              { title: '是否最新', dataIndex: 'is_latest', key: 'is_latest', render: (v: boolean) => v ? <Tag color="green">是</Tag> : <Tag>否</Tag> },
            ]}
            dataSource={allVersions.map((v, idx) => ({ ...v, key: idx }))}
            pagination={false}
            size="small"
          />
        </Card>
      )}
    </Layout>
  );
}

export default McpDetail;
