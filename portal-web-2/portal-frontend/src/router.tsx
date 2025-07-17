import { Routes, Route } from "react-router-dom";
import Home from "./pages/Home";
import Apis from "./pages/Apis";
import ApiDetail from "./pages/ApiDetail";
import Consumers from "./pages/Consumers";

export function Router() {
  return (
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/apis" element={<Apis />} />
      <Route path="/apis/:id" element={<ApiDetail />} />
      <Route path="/consumers" element={<Consumers />} />
      {/* 其他页面可继续添加 */}
    </Routes>
  );
} 