package com.ruhuo.xuaizerobackend.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.ruhuo.xuaizerobackend.ai.model.HtmlCodeResult;
import com.ruhuo.xuaizerobackend.ai.model.MultiFileCodeResult;
import com.ruhuo.xuaizerobackend.exception.BusinessException;
import com.ruhuo.xuaizerobackend.exception.ErrorCode;
import com.ruhuo.xuaizerobackend.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.prefs.BackingStoreException;

/**
 * 抽象代码文件保存器 - 模板方法模式
 *
 * public：对外开放，谁都能调。
 * private：私有，只有自己能用，儿子（子类）都不能用。
 * protected：传家宝。不对外人开放，但专门留给“儿子”（子类）使用或重写。
 *
 */
public abstract class CodeFileSaverTemplate<T> {

    protected static final String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir")+"/tmp/code_output";

    /**
     * 模板方法：保存代码的标准流程（使用appId）
     *
     * @param result 代码结果对象
     * @param appId 应用ID
     * @return 保存的目录
     */
    public final File saveCode(T result, Long appId){
        // 1. 验证输入
        validateInput(result);

        // 2. 构建基于 appId 的目录
        String baseDirPath = buildUniqueDir(appId);

        // 3. 保存文件，具体实现由子类提供
        saveFiles(result,baseDirPath);

        // 4. 返回目录文件对象
        return new File(baseDirPath);
    }

    /**
     * 验证输入参数（可由子类覆盖）
     *
     * @param result 代码结果对象
     */
    protected void validateInput(T result){
        if(result == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"代码结果对象不能为空");
        }
    }

    /**
     * 构建基于 appId 的目录路径
     *
     * @return 目录路径
     */
    protected final String buildUniqueDir(Long appId){
        if (appId == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"应用 ID 不能为空");
        }

        String codeType = getCodeType().getValue();
        String uniqueDirName = StrUtil.format("{}_{}",codeType, appId);
        String dirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;

        // 真正去硬盘上创建这个文件夹
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    /**
     * 写入单个文件的工具方法
     *
     * @param dirPath 目录路径
     * @param filename 文件名
     * @param content 文件内容
     */
    protected final void writeToFile(String dirPath,String filename,String content){
        if(StrUtil.isNotBlank(content)){
            String filePath = dirPath + File.separator + filename;
            // 使用 UTF-8 编码写入，防止中文乱码
            FileUtil.writeString(content,filePath, StandardCharsets.UTF_8);
        }
    }

    /**
     * 获取代码类型（由子类实现）
     *
     * @return 代码生成类型
     */
    protected abstract CodeGenTypeEnum getCodeType();

    /**
     * 保存文件的具体实现（由子类实现）
     *
     * @param result        代码结果对象
     * @param baseDirPath   基础目录路径
     */
    protected abstract void saveFiles(T result,String baseDirPath);
}
