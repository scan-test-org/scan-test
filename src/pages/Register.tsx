import React, { useState } from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
// import aliyunIcon from '../assets/aliyun.png'
// import githubIcon from '../assets/github.png'
// import googleIcon from '../assets/google.png'
import api from '../lib/api'

const Register: React.FC = () => {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const navigate = useNavigate()
  const location = useLocation()
  const searchParams = new URLSearchParams(location.search)
  const portalId = searchParams.get('portalId') || ''

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    if (!username || !password || !confirmPassword) {
      setError('请填写所有字段')
      return
    }
    if (password !== confirmPassword) {
      setError('两次输入的密码不一致')
      return
    }
    setLoading(true)
    try {
      // 这里需要根据实际API调整
      await api.post('/api/developer/register', { username, password, portalId })
      // 注册成功后跳转到登录页
      navigate('/login')
    } catch {
      setError('注册失败')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="flex items-center justify-center min-h-screen bg-white">
      <div className="bg-white p-8 rounded-xl shadow-2xl w-full max-w-md flex flex-col items-center border border-gray-100">
        {/* Logo */}
        <div className="mb-4">
          <span className="block text-4xl text-gray-900 text-center">⬛</span>
        </div>
        <h2 className="text-2xl font-bold mb-6 text-gray-900 text-center">注册 API Portal</h2>
        <form className="w-full flex flex-col gap-4" onSubmit={handleRegister}>
          <input
            className="px-4 py-2 rounded bg-gray-100 text-gray-900 placeholder-gray-400 border border-gray-200 focus:outline-none focus:ring-2 focus:ring-indigo-400"
            type="text"
            placeholder="账号"
            value={username}
            onChange={e => setUsername(e.target.value)}
            autoComplete="username"
          />
          <input
            className="px-4 py-2 rounded bg-gray-100 text-gray-900 placeholder-gray-400 border border-gray-200 focus:outline-none focus:ring-2 focus:ring-indigo-400"
            type="password"
            placeholder="密码"
            value={password}
            onChange={e => setPassword(e.target.value)}
            autoComplete="new-password"
          />
          <input
            className="px-4 py-2 rounded bg-gray-100 text-gray-900 placeholder-gray-400 border border-gray-200 focus:outline-none focus:ring-2 focus:ring-indigo-400"
            type="password"
            placeholder="确认密码"
            value={confirmPassword}
            onChange={e => setConfirmPassword(e.target.value)}
            autoComplete="new-password"
          />
          {error && <div className="text-red-500 text-sm text-center">{error}</div>}
          <button
            type="submit"
            className="w-full py-2 rounded bg-gray-900 text-white font-semibold hover:bg-gray-800 transition-colors disabled:opacity-60"
            disabled={loading}
          >
            {loading ? '注册中...' : '注册'}
          </button>
        </form>
        <div className="mt-6 text-gray-400 text-sm text-center w-full">
          已有账号？<Link to="/login" className="text-indigo-500 hover:underline ml-1">登录</Link>
        </div>
      </div>
    </div>
  )
}

export default Register 