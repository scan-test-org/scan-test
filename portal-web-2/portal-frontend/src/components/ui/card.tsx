import React from "react";
import { Card as AntdCard } from "antd";
import type { CardProps as AntdCardProps } from "antd";
import clsx from "clsx";

export const Card: React.FC<AntdCardProps & { className?: string }> = ({ className, ...props }) => (
  <AntdCard className={clsx(className)} {...props} />
); 