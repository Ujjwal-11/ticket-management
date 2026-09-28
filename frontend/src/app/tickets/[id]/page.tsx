import { TicketScreen } from "../../../components/TicketScreen";

export default async function TicketPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  return <TicketScreen id={id} />;
}
