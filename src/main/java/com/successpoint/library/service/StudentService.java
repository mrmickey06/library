package com.successpoint.library.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.successpoint.library.entity.Student;
import com.successpoint.library.repository.StudentRepository;

@Service
public class StudentService {
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final String FEE_CLEAR = "CLEAR";
    private static final String FEE_PENDING = "PENDING";

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private EmailService emailService;

    public Student registerStudent(Student student) {
        return registerStudent(student, FEE_CLEAR);
    }

    public Student registerStudent(Student student, String feeStatus) {
        LocalDate today = LocalDate.now();
        String normalizedFeeStatus = normalizeFeeStatus(feeStatus);

        student.setJoiningDate(today);
        student.setOriginalJoiningDate(today);
        student.setLastRenewalDate(null);
        student.setRenewalCount(0);
        student.setDueDate(today.plusMonths(student.getFeesPeriodMonths()));
        student.setPassword("SP@123");
        student.setApplicationStatus(STATUS_APPROVED);
        student.setFeeStatus(normalizedFeeStatus);

        if (FEE_CLEAR.equals(normalizedFeeStatus)) {
            student.setTotalFeesCollected(student.getFeesPaid());
            Student savedStudent = studentRepository.save(student);
            emailService.sendInvoiceEmail(savedStudent);
            return savedStudent;
        }

        student.setTotalFeesCollected(0);
        Student savedStudent = studentRepository.save(student);
        emailService.sendPendingFeesEmail(savedStudent);
        return savedStudent;
    }

    public Student submitApplication(Student student) {
        student.setJoiningDate(null);
        student.setOriginalJoiningDate(null);
        student.setLastRenewalDate(null);
        student.setRenewalCount(0);
        student.setTotalFeesCollected(0);
        student.setDueDate(null);
        student.setPassword("SP@123");
        student.setApplicationStatus(STATUS_PENDING);
        student.setFeeStatus(FEE_PENDING);
        return studentRepository.save(student);
    }

    public Student approveApplication(Long id, Student formStudent, String feeStatus) {
        Student existingStudent = getStudentById(id);
        if (existingStudent == null) {
            return null;
        }

        updateStudentDetails(existingStudent, formStudent);

        LocalDate today = LocalDate.now();
        existingStudent.setApplicationStatus(STATUS_APPROVED);
        existingStudent.setFeeStatus(normalizeFeeStatus(feeStatus));
        existingStudent.setJoiningDate(today);
        if (existingStudent.getOriginalJoiningDate() == null) {
            existingStudent.setOriginalJoiningDate(today);
        }
        existingStudent.setLastRenewalDate(null);
        existingStudent.setRenewalCount(0);
        existingStudent.setDueDate(today.plusMonths(existingStudent.getFeesPeriodMonths()));
        existingStudent.setPassword("SP@123");

        if (FEE_CLEAR.equals(existingStudent.getFeeStatus())) {
            existingStudent.setTotalFeesCollected(existingStudent.getFeesPaid());
            Student savedStudent = studentRepository.save(existingStudent);
            emailService.sendInvoiceEmail(savedStudent);
            return savedStudent;
        }

        existingStudent.setTotalFeesCollected(0);
        Student savedStudent = studentRepository.save(existingStudent);
        emailService.sendPendingFeesEmail(savedStudent);
        return savedStudent;
    }

    public Student rejectApplication(Long id, Student formStudent) {
        Student existingStudent = getStudentById(id);
        if (existingStudent == null) {
            return null;
        }

        updateStudentDetails(existingStudent, formStudent);
        existingStudent.setApplicationStatus(STATUS_REJECTED);
        existingStudent.setFeeStatus(FEE_PENDING);
        existingStudent.setJoiningDate(null);
        existingStudent.setOriginalJoiningDate(null);
        existingStudent.setLastRenewalDate(null);
        existingStudent.setRenewalCount(0);
        existingStudent.setTotalFeesCollected(0);
        existingStudent.setDueDate(null);
        return studentRepository.save(existingStudent);
    }

    public Student renewStudent(Long id, int feesPeriodMonths, double feesPaid) {
        Student student = studentRepository.findById(id).orElse(null);
        if (student == null) {
            return null;
        }

        LocalDate today = LocalDate.now();

        student.setJoiningDate(today);
        student.setLastRenewalDate(today);
        student.setDueDate(today.plusMonths(feesPeriodMonths));
        student.setFeesPeriodMonths(feesPeriodMonths);
        student.setFeesPaid(feesPaid);
        student.setFeeStatus(FEE_CLEAR);
        student.setRenewalCount(student.getRenewalCount() + 1);
        student.setTotalFeesCollected(student.getTotalFeesCollected() + feesPaid);

        Student savedStudent = studentRepository.save(student);
        emailService.sendInvoiceEmail(savedStudent);
        return savedStudent;
    }

    public List<Student> getAllStudents() {
        List<Student> approvedStudents = new ArrayList<>();
        for (Student student : studentRepository.findAll()) {
            if (isApproved(student)) {
                approvedStudents.add(student);
            }
        }
        return approvedStudents;
    }

    public List<Student> getPendingApplications() {
        return studentRepository.findByApplicationStatus(STATUS_PENDING);
    }

    public List<Student> getPendingFeeStudents() {
        return studentRepository.findByApplicationStatusAndFeeStatus(STATUS_APPROVED, FEE_PENDING);
    }

    public Student getStudentById(Long id) {
        return studentRepository.findById(id).orElse(null);
    }

    public Student updateStudent(Student student) {
        return studentRepository.save(student);
    }

    public Student updateStudentProfile(Long id, Student formStudent) {
        Student existingStudent = getStudentById(id);
        if (existingStudent == null) {
            return null;
        }

        double previousFeesPaid = existingStudent.getFeesPaid();
        int previousMonths = existingStudent.getFeesPeriodMonths();
        String previousFeeStatus = existingStudent.getFeeStatus();

        updateStudentDetails(existingStudent, formStudent);

        if (formStudent.getFeeStatus() != null) {
            existingStudent.setFeeStatus(normalizeFeeStatus(formStudent.getFeeStatus()));
        }

        if (existingStudent.getJoiningDate() != null && existingStudent.getFeesPeriodMonths() > 0) {
            existingStudent.setDueDate(existingStudent.getJoiningDate().plusMonths(existingStudent.getFeesPeriodMonths()));
        }

        if (FEE_PENDING.equals(existingStudent.getFeeStatus())) {
            existingStudent.setTotalFeesCollected(0);
        } else if (existingStudent.getTotalFeesCollected() < existingStudent.getFeesPaid()) {
            existingStudent.setTotalFeesCollected(existingStudent.getFeesPaid());
        }

        Student savedStudent = studentRepository.save(existingStudent);
        boolean feesChanged = Double.compare(previousFeesPaid, savedStudent.getFeesPaid()) != 0
                || previousMonths != savedStudent.getFeesPeriodMonths();
        boolean statusChanged = previousFeeStatus == null
                ? savedStudent.getFeeStatus() != null
                : !previousFeeStatus.equalsIgnoreCase(savedStudent.getFeeStatus());

        if (feesChanged || statusChanged) {
            if (FEE_PENDING.equals(savedStudent.getFeeStatus())) {
                emailService.sendPendingFeesEmail(savedStudent);
            } else {
                emailService.sendInvoiceEmail(savedStudent);
            }
        }

        return savedStudent;
    }

    public void deleteStudent(Long id) {
        studentRepository.deleteById(id);
    }

    public List<Student> getStudentsWithDues() {
        return studentRepository.findByApplicationStatusAndDueDateBefore(STATUS_APPROVED, LocalDate.now());
    }

    public List<Student> getMonthlyEarningStudents(YearMonth month) {
        LocalDate startDate = month.atDay(1);
        LocalDate endDate = month.atEndOfMonth();

        List<Student> students = studentRepository.findByJoiningDateBetween(startDate, endDate);
        List<Student> earningStudents = new ArrayList<>();
        for (Student student : students) {
            if (isApproved(student) && FEE_CLEAR.equalsIgnoreCase(student.getFeeStatus())) {
                earningStudents.add(student);
            }
        }
        return earningStudents;
    }

    public double getMonthlyEarningsTotal(YearMonth month) {
        double total = 0;
        for (Student student : getMonthlyEarningStudents(month)) {
            total += student.getFeesPaid();
        }
        return total;
    }

    public Student clearPendingFees(Long id) {
        Student student = getStudentById(id);
        if (student == null) {
            return null;
        }

        student.setFeeStatus(FEE_CLEAR);
        if (student.getTotalFeesCollected() < student.getFeesPaid()) {
            student.setTotalFeesCollected(student.getFeesPaid());
        }
        Student savedStudent = studentRepository.save(student);
        emailService.sendInvoiceEmail(savedStudent);
        return savedStudent;
    }

    public boolean canStudentLogin(Student student) {
        return student != null && isApproved(student);
    }

    private void updateStudentDetails(Student existingStudent, Student formStudent) {
        existingStudent.setName(formStudent.getName());
        existingStudent.setMobileNumber(formStudent.getMobileNumber());
        existingStudent.setEmail(formStudent.getEmail());
        existingStudent.setFeesPeriodMonths(formStudent.getFeesPeriodMonths());
        existingStudent.setFeesPaid(formStudent.getFeesPaid());
    }

    private boolean isApproved(Student student) {
        return student.getApplicationStatus() == null || STATUS_APPROVED.equalsIgnoreCase(student.getApplicationStatus());
    }

    private String normalizeFeeStatus(String feeStatus) {
        if (FEE_PENDING.equalsIgnoreCase(feeStatus)) {
            return FEE_PENDING;
        }
        return FEE_CLEAR;
    }
}
