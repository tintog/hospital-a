package com.hospital.appointment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.appointment.common.dto.MemberReq;
import com.hospital.appointment.common.dto.PasswordUpdateReq;
import com.hospital.appointment.common.dto.PhoneUpdateReq;
import com.hospital.appointment.common.exception.BusinessException;
import com.hospital.appointment.common.result.Result;
import com.hospital.appointment.entity.Patient;
import com.hospital.appointment.entity.PatientMember;
import com.hospital.appointment.mapper.PatientMapper;
import com.hospital.appointment.mapper.PatientMemberMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class PatientService {

    @Autowired
    private PatientMapper patientMapper;

    @Autowired
    private PatientMemberMapper memberMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Result<Patient> getProfile(Long patientId) {
        Patient patient = patientMapper.selectById(patientId);
        if (patient == null) {
            return Result.error(404, "患者不存在");
        }
        patient.setPassword(null);
        return Result.success(patient);
    }

    public Result<List<PatientMember>> getMembers(Long patientId) {
        List<PatientMember> members = memberMapper.selectList(
                new LambdaQueryWrapper<PatientMember>()
                        .eq(PatientMember::getPatientId, patientId)
                        .orderByDesc(PatientMember::getCreatedAt));
        return Result.success(members);
    }

    public Result<String> updatePhone(Long patientId, PhoneUpdateReq req) {
        Patient patient = patientMapper.selectById(patientId);
        if (patient == null) {
            throw new BusinessException("患者不存在");
        }
        if (req.getNewPhone().equals(patient.getPhone())) {
            throw new BusinessException("新手机号不能与当前手机号一致");
        }

        Patient exists = patientMapper.selectOne(
                new LambdaQueryWrapper<Patient>()
                        .eq(Patient::getPhone, req.getNewPhone()));
        if (exists != null && !exists.getId().equals(patientId)) {
            throw new BusinessException("该手机号已被使用");
        }

        patient.setPhone(req.getNewPhone());
        patientMapper.updateById(patient);
        return Result.success("手机号修改成功", null);
    }

    public Result<String> updatePassword(Long patientId, PasswordUpdateReq req) {
        Patient patient = patientMapper.selectById(patientId);
        if (patient == null) {
            throw new BusinessException("患者不存在");
        }
        if (!passwordEncoder.matches(req.getOldPassword(), patient.getPassword())) {
            throw new BusinessException("旧密码错误");
        }
        if (req.getOldPassword().equals(req.getNewPassword())) {
            throw new BusinessException("新密码不能与旧密码相同");
        }

        patient.setPassword(passwordEncoder.encode(req.getNewPassword()));
        patientMapper.updateById(patient);
        return Result.success("密码修改成功，请重新登录", null);
    }

    public Result<PatientMember> addMember(Long patientId, MemberReq req) {
        PatientMember exists = memberMapper.selectOne(
                new LambdaQueryWrapper<PatientMember>()
                        .eq(PatientMember::getPatientId, patientId)
                        .eq(PatientMember::getIdCard, req.getIdCard()));
        if (exists != null) {
            throw new BusinessException("该身份证号已添加");
        }

        PatientMember member = new PatientMember();
        member.setPatientId(patientId);
        member.setName(req.getName());
        member.setIdCard(req.getIdCard());
        member.setRelation(req.getRelation());
        member.setAuthStatus(1);
        memberMapper.insert(member);
        return Result.success("添加成功", member);
    }

    public Result<String> deleteMember(Long patientId, Long memberId) {
        PatientMember member = memberMapper.selectById(memberId);
        if (member == null || !member.getPatientId().equals(patientId)) {
            throw new BusinessException("无权操作");
        }
        memberMapper.deleteById(memberId);
        return Result.success("删除成功", null);
    }
}
