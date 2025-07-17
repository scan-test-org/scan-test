import React from "react";
import { Input as AntdInput } from "antd";
import type { InputProps as AntdInputProps } from "antd";
import clsx from "clsx";

export const Input: React.FC<AntdInputProps & { className?: string }> = ({ className, ...props }) => (
  <AntdInput className={clsx(className)} {...props} />
); 