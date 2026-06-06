export default function PlaceholderPage({ title, description }: { title: string; description: string }) {
  return (
    <div className="flex-1 space-y-3 p-6 md:p-8">
      <h1 className="text-2xl font-semibold tracking-tight">{title}</h1>
      <p className="text-sm text-muted-foreground">{description}</p>
    </div>
  )
}

