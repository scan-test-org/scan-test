import React from "react";
import { Button as AntdButton } from "antd";
import type { ButtonProps as AntdButtonProps } from "antd";
import clsx from "clsx";

export interface ButtonProps extends AntdButtonProps {
  className?: string;
}

export const Button: React.FC<ButtonProps> = ({ className, ...props }) => (
  <AntdButton className={clsx(className, "rounded-md")} {...props} />
); 