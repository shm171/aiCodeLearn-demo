import assert from 'node:assert/strict'
import test from 'node:test'

import {
  buildSubmissionErrorCounts,
  cleanSubmissionCount,
  errorCountForSubmission,
} from '../src/utils/submissionStats.js'

test('按提交 ID 汇总错题数并计算无错题提交数', () => {
  const counts = buildSubmissionErrorCounts([
    { sourceFileId: 101 },
    { sourceFileId: 101 },
    { sourceFileId: 102 },
    { sourceFileId: null },
  ])

  assert.equal(errorCountForSubmission(counts, 101), 2)
  assert.equal(errorCountForSubmission(counts, '102'), 1)
  assert.equal(errorCountForSubmission(counts, 103), 0)
  assert.equal(cleanSubmissionCount(4, counts), 2)
})
