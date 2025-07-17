import React from "react";
import { Modal } from "antd";
import type { ModalProps } from "antd";
import clsx from "clsx";

export const Dialog: React.FC<ModalProps & { className?: string }> = ({ className, ...props }) => (
  <Modal className={clsx(className)} {...props} />
); 