package uz.pdp.task2filesystemuploaddownload.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import uz.pdp.task2filesystemuploaddownload.entity.Attachment;
import uz.pdp.task2filesystemuploaddownload.repository.AttachmentRepository;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/attachment")
public class AttachmentController {

    @Autowired
    AttachmentRepository attachmentRepository;

    private final String uploadDirectory = "uploadedFiles";

    @PostMapping("/uploadSystem")
    public String uploadFileToFileSystem(MultipartHttpServletRequest request) {
        Iterator<String> filesNames = request.getFileNames();
        MultipartFile file = request.getFile(filesNames.next());

        if (file != null) {
            String originalFileName = file.getOriginalFilename();
            long size = file.getSize();
            String contentType = file.getContentType();

            Attachment attachment = new Attachment();
            attachment.setOriginalFileName(originalFileName);
            attachment.setSize(size);
            attachment.setContentType(contentType);

            String[] strings = originalFileName.split("\\.");

            String name = UUID.randomUUID().toString() + "." + strings[strings.length - 1];
            attachment.setName(name);
            Attachment savedAttachment = attachmentRepository.save(attachment);
            Path path= Paths.get(uploadDirectory+"/"+name);
            try {
                Files.copy(file.getInputStream(),path);
                return "File saved.  File Id : " + savedAttachment.getId();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return "The file was not saved";
    }

    @GetMapping("/download/{id}")
    public void getFileFromFileSystem(@PathVariable Integer id, HttpServletResponse response) {
        Optional<Attachment> optionalAttachment = attachmentRepository.findById(id);
        if (optionalAttachment.isPresent()) {
            Attachment attachment = optionalAttachment.get();
                response.setHeader("Content-Disposition",
                        "attachment; filename=\"" + attachment.getOriginalFileName() + "\"");

                response.setContentType(attachment.getContentType());

                try {
                    FileInputStream fileInputStream = new FileInputStream(uploadDirectory+"/"+attachment.getName());

                    FileCopyUtils.copy(fileInputStream, response.getOutputStream());
                } catch (IOException e) {
                    System.out.println("Could not find this file "+attachment.getOriginalFileName()+".");
                }


        }

    }
}
