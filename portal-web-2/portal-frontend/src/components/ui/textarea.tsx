import React from "react";
import { Input } from "antd";
import type { TextAreaProps } from "antd/es/input";
import clsx from "clsx";

export const Textarea: React.FC<TextAreaProps & { className?: string }> = ({ className, ...props }) => (
  <Input.TextArea className={clsx(className)} {...props} />
); 