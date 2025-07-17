import React from "react";
import { Dropdown, Menu } from "antd";
import type { MenuProps } from "antd";
import clsx from "clsx";

export const DropdownMenu: React.FC<MenuProps & { className?: string; trigger?: React.ReactNode }> = ({ className, trigger, ...props }) => (
  <Dropdown overlay={<Menu className={clsx(className)} {...props} />} trigger={["click"]}>
    {trigger}
  </Dropdown>
); 