import React from "react";
import { Select as AntdSelect } from "antd";
import type { SelectProps as AntdSelectProps } from "antd";
import clsx from "clsx";

export const Select: React.FC<AntdSelectProps<any> & { className?: string }> = ({ className, ...props }) => (
  <AntdSelect className={clsx(className)} {...props} />
); 