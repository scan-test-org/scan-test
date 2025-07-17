import React from "react";
import { Table as AntdTable } from "antd";
import type { TableProps as AntdTableProps } from "antd";
import clsx from "clsx";

export function Table<T extends object>(props: AntdTableProps<T>) {
  return (
    <div className={clsx("w-full overflow-auto", props.className)}>
      <AntdTable {...props} />
    </div>
  );
} 