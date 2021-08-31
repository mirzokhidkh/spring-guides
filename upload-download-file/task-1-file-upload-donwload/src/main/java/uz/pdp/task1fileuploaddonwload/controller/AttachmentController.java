package uz.pdp.task1fileuploaddonwload.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import uz.pdp.task1fileuploaddonwload.entity.Attachment;
import uz.pdp.task1fileuploaddonwload.entity.AttachmentContent;
import uz.pdp.task1fileuploaddonwload.repository.AttachmentContentRepository;
import uz.pdp.task1fileuploaddonwload.repository.AttachmentRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/attachment")
public class AttachmentController {

    @Autowired
    AttachmentRepository attachmentRepository;

    @Autowired
    AttachmentContentRepository attachmentContentRepository;

    @GetMapping("/info")
    public List<Attachment> getAll() {
        List<Attachment> attachments = attachmentRepository.findAll();
        return attachments;
    }


    @GetMapping("/info/{id}")
    public Attachment getOne(@PathVariable Integer id) {
        Optional<Attachment> optionalAttachment = attachmentRepository.findById(id);
        if (optionalAttachment.isEmpty()) {
            return new Attachment();
        }
        return optionalAttachment.get();
    }


    @PostMapping("/upload")
    public String upload(MultipartHttpServletRequest request) {
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
            Attachment savedAttachment = attachmentRepository.save(attachment);

            AttachmentContent attachmentContent = new AttachmentContent();
            try {
                attachmentContent.setBasicContent(file.getBytes());
                attachmentContent.setAttachment(savedAttachment);
                attachmentContentRepository.save(attachmentContent);
                return "File saved.  File Id : " + savedAttachment.getId();
            } catch (IOException e) {
                return "Could not store file "+file.getOriginalFilename()+". Please try again!";
            }
        }

        return "File is empty";
    }

    @GetMapping("/download/{id}")
    public void download(@PathVariable Integer id, HttpServletResponse response) {
        Optional<Attachment> optionalAttachment = attachmentRepository.findById(id);
        if (optionalAttachment.isPresent()) {
            Attachment attachment = optionalAttachment.get();
            Optional<AttachmentContent> contentOptional = attachmentContentRepository.findByAttachmentId(id);
            if (contentOptional.isPresent()) {
                AttachmentContent attachmentContent = contentOptional.get();

                response.setHeader("Content-Disposition",
                        "attachment; filename=\"" + attachment.getOriginalFileName() + "\"");

                response.setContentType(attachment.getContentType());

                try {
                    FileCopyUtils.copy(attachmentContent.getBasicContent(), response.getOutputStream());
                } catch (IOException e) {
                    System.out.println("Something went wrong");
                }
            }

        }

    }
}
