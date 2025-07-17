import React from "react";
import { Avatar as AntdAvatar } from "antd";
import type { AvatarProps as AntdAvatarProps } from "antd";
import clsx from "clsx";

export const Avatar: React.FC<AntdAvatarProps & { className?: string }> = ({ className, ...props }) => (
  <AntdAvatar className={clsx(className)} {...props} />
); 