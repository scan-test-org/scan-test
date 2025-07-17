import React from "react";
import clsx from "clsx";

export const Label: React.FC<React.LabelHTMLAttributes<HTMLLabelElement>> = ({ className, ...props }) => (
  <label className={clsx("block text-sm font-medium", className)} {...props} />
); 