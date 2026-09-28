import type { ReactNode } from "react";
import "./globals.css";

export const metadata = {
  title: "Support tickets",
  description: "Shared support ticket queue",
};

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="en">
      <body>
        <main>{children}</main>
      </body>
    </html>
  );
}
