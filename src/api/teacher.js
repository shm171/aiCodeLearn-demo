import request from "@/utils/request";
import {
  mockCreateQuestion,
  mockDeleteQuestion,
  mockExportWrongQuestions,
  mockGetQuestionList,
  mockGetReviewDetail,
  mockGetStudentList,
  mockGetStudentReport,
  mockGetSubmissionList,
  mockGetWrongQuestions,
  mockGradeSubmission,
  mockUpdateQuestion,
  mockUpdateQuestionStatus
} from "./mock";
const USE_MOCK = import.meta.env.VITE_USE_MOCK === "true";
function getQuestionList(params) {
  if (USE_MOCK) return mockGetQuestionList(params);
  return request.get("/core/question/list", { params });
}
function createQuestion(data) {
  if (USE_MOCK) return mockCreateQuestion(data);
  return request.post("/core/question", data);
}
function updateQuestion(id, data) {
  if (USE_MOCK) return mockUpdateQuestion(id, data);
  return request.put(`/core/question/${id}`, data);
}
function updateQuestionStatus(id, status) {
  if (USE_MOCK) return mockUpdateQuestionStatus(id, status);
  return request.put(`/core/question/${id}/status`, { status });
}
function deleteQuestion(id) {
  if (USE_MOCK) return mockDeleteQuestion(id);
  return request.delete(`/core/question/${id}`);
}
function getSubmissionList(params) {
  if (USE_MOCK) return mockGetSubmissionList(params);
  return request.get("/teacher/submissions", { params });
}
function getReviewDetail(id) {
  if (USE_MOCK) return mockGetReviewDetail(id);
  return request.get(`/teacher/submissions/${id}`);
}
function gradeSubmission(submissionId, enableLLM = false) {
  if (USE_MOCK) return mockGradeSubmission(submissionId, enableLLM);
  return request.post(`/core/submissions/${submissionId}/grade`, { enableLLM });
}
function getWrongQuestions(params) {
  if (USE_MOCK) return mockGetWrongQuestions(params);
  return request.get("/teacher/wrong-questions", { params });
}
function exportWrongQuestions() {
  if (USE_MOCK) return mockExportWrongQuestions();
  return request.get("/teacher/wrong-questions/export", { responseType: "blob" });
}
function getStudentList(params) {
  if (USE_MOCK) return mockGetStudentList(params);
  return request.get("/teacher/students", { params });
}
function getStudentReport(id) {
  if (USE_MOCK) return mockGetStudentReport(id);
  return request.get(`/teacher/students/${id}/report`);
}
export {
  createQuestion,
  deleteQuestion,
  exportWrongQuestions,
  getQuestionList,
  getReviewDetail,
  getStudentList,
  getStudentReport,
  getSubmissionList,
  getWrongQuestions,
  gradeSubmission,
  updateQuestion,
  updateQuestionStatus
};
