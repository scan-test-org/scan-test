import React from "react";
import { Badge as AntdBadge } from "antd";
import type { BadgeProps as AntdBadgeProps } from "antd";
import clsx from "clsx";

export const Badge: React.FC<AntdBadgeProps & { className?: string }> = ({ className, ...props }) => (
  <AntdBadge className={clsx(className)} {...props} />
); 