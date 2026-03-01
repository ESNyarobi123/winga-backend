import { useEffect, useState } from "react";
import { Card, CardBody, CardHeader } from "@heroui/react";
import { Button } from "@heroui/react";
import { getProposalsForModeration, moderateProposal, type ProposalRow } from "../api/client";

export default function BidsModeration() {
  const [proposals, setProposals] = useState<ProposalRow[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [acting, setActing] = useState<number | null>(null);

  const load = () => {
    setLoading(true);
    getProposalsForModeration(0, 50)
      .then((res) => {
        const data = res.data as { content?: ProposalRow[] };
        setProposals(data?.content ?? []);
      })
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  };

  useEffect(() => load(), []);

  const handleModerate = (id: number, status: "APPROVED" | "REJECTED") => {
    setActing(id);
    moderateProposal(id, status)
      .then(() => load())
      .catch((e) => setError(e.message))
      .finally(() => setActing(null));
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-800 dark:text-slate-100">Bids moderation</h1>
        <p className="text-default-500">Approve or reject bids (proposals) before they are visible to clients</p>
      </div>
      {error && <p className="text-danger text-sm">{error}</p>}
      <Card className="border border-slate-200 dark:border-slate-700 shadow-sm">
        <CardHeader className="border-b border-slate-200 dark:border-slate-700">
          <h3 className="font-semibold">Pending approval</h3>
        </CardHeader>
        <CardBody>
          {loading ? (
            <div className="py-8 text-center text-default-500">Loading…</div>
          ) : proposals.length === 0 ? (
            <p className="text-default-500 py-8 text-center">No bids pending moderation.</p>
          ) : (
            <ul className="divide-y divide-default-200">
              {proposals.map((p) => (
                <li key={p.id} className="py-4 flex flex-wrap items-center justify-between gap-4">
                  <div className="min-w-0 flex-1">
                    <p className="font-medium truncate">{p.jobTitle}</p>
                    <p className="text-sm text-default-500">
                      Job #{p.jobId} · Bid: {typeof p.bidAmount === "number" ? p.bidAmount.toLocaleString() : p.bidAmount} TZS
                    </p>
                    {p.coverLetter && (
                      <p className="text-sm text-default-400 mt-1 line-clamp-2">{p.coverLetter}</p>
                    )}
                  </div>
                  <div className="flex gap-2 shrink-0">
                    <Button
                      size="sm"
                      color="success"
                      isLoading={acting === p.id}
                      onPress={() => handleModerate(p.id, "APPROVED")}
                    >
                      Approve
                    </Button>
                    <Button
                      size="sm"
                      color="danger"
                      variant="flat"
                      isLoading={acting === p.id}
                      onPress={() => handleModerate(p.id, "REJECTED")}
                    >
                      Reject
                    </Button>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </CardBody>
      </Card>
    </div>
  );
}
