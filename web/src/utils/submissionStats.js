export function buildSubmissionErrorCounts(errors = []) {
  const counts = new Map()
  for (const error of errors) {
    if (error?.sourceFileId == null) continue
    const submissionId = String(error.sourceFileId)
    counts.set(submissionId, (counts.get(submissionId) || 0) + 1)
  }
  return counts
}

export function errorCountForSubmission(counts, submissionId) {
  if (submissionId == null) return 0
  return counts.get(String(submissionId)) || 0
}

export function cleanSubmissionCount(totalSubmissions, counts) {
  return Math.max(0, Number(totalSubmissions || 0) - counts.size)
}
