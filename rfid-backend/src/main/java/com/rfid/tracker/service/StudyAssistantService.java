package com.rfid.tracker.service;

import com.rfid.tracker.dto.CreateMindMapRequest;
import com.rfid.tracker.dto.CreateMindMapResponse;
import com.rfid.tracker.dto.MindMapDTO;
import com.rfid.tracker.entity.MindMap;
import com.rfid.tracker.repository.MindMapRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StudyAssistantService {

    private static final Logger logger = LoggerFactory.getLogger(StudyAssistantService.class);

    @Autowired
    private MindMapRepository mindMapRepository;

    @Autowired
    private OllamaService ollamaService;

    /**
     * Create a new mind map with async processing
     */
    public CreateMindMapResponse createMindMap(CreateMindMapRequest request, Long studentId) {
        try {
            // Generate unique ID for this mind map
            String mindMapId = UUID.randomUUID().toString();

            // Create new MindMap entity
            MindMap mindMap = new MindMap();
            mindMap.setMindMapId(mindMapId);
            mindMap.setStudentId(studentId);
            
            // Extract values from request - handle nulls
            String videoUrl = getVideoUrl(request);
            String subject = getSubject(request);
            
            mindMap.setVideoUrl(videoUrl);
            mindMap.setVideoTitle(extractVideoTitle(videoUrl));
            mindMap.setSubject(subject);
            mindMap.setStatus("PENDING");

            // Save to database
            MindMap savedMindMap = mindMapRepository.save(mindMap);

            // Start async processing
            processMindMapAsync(savedMindMap.getMindMapId(), videoUrl, subject);

            // Return response
            return new CreateMindMapResponse(
                    true,
                    "Mind map creation started. Processing...",
                    savedMindMap.getMindMapId()
            );

        } catch (Exception e) {
            logger.error("Error creating mind map: " + e.getMessage(), e);
            return new CreateMindMapResponse(
                    false,
                    "Error creating mind map: " + e.getMessage(),
                    null
            );
        }
    }

    /**
     * Async processing of mind map
     */
    @Async
    public void processMindMapAsync(String mindMapId, String videoUrl, String subject) {
        try {
            // Update status to PROCESSING
            Optional<MindMap> optionalMindMap = mindMapRepository.findById(mindMapId);
            if (optionalMindMap.isPresent()) {
                MindMap mindMap = optionalMindMap.get();
                mindMap.setStatus("PROCESSING");
                mindMapRepository.save(mindMap);
            }

            // Mock transcript extraction (in production, use YouTube API)
            String transcript = extractMockTranscript(videoUrl, subject);

            // Generate mind map using Ollama
            String mindMapJson = ollamaService.generateMindMap(transcript, subject);

            // Parse and extract key points
            String keyPoints = extractKeyPoints(mindMapJson);
            String summary = extractSummary(mindMapJson);

            // Update mind map with results
            Optional<MindMap> mindMapToUpdate = mindMapRepository.findById(mindMapId);
            if (mindMapToUpdate.isPresent()) {
                MindMap mindMap = mindMapToUpdate.get();
                mindMap.setMindMapJson(mindMapJson);
                mindMap.setKeyPoints(keyPoints);
                mindMap.setSummary(summary);
                mindMap.setStatus("COMPLETED");
                mindMapRepository.save(mindMap);
                logger.info("Mind map {} processing completed", mindMapId);
            }

        } catch (Exception e) {
            logger.error("Error processing mind map {}: {}", mindMapId, e.getMessage(), e);
            // Update status to FAILED
            try {
                Optional<MindMap> mindMapToUpdate = mindMapRepository.findById(mindMapId);
                if (mindMapToUpdate.isPresent()) {
                    MindMap mindMap = mindMapToUpdate.get();
                    mindMap.setStatus("FAILED");
                    mindMapRepository.save(mindMap);
                }
            } catch (Exception innerEx) {
                logger.error("Error updating mind map status to FAILED: {}", innerEx.getMessage());
            }
        }
    }

    /**
     * Get all mind maps for a student
     */
    public List<MindMapDTO> getStudentMindMaps(Long studentId) {
        try {
            List<MindMap> mindMaps = mindMapRepository.findByStudentId(studentId);
            return mindMaps.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching mind maps for student {}: {}", studentId, e.getMessage());
            return List.of();
        }
    }

    /**
     * Get single mind map by ID
     */
    public MindMapDTO getMindMapById(String mindMapId) {
        try {
            Optional<MindMap> mindMap = mindMapRepository.findById(mindMapId);
            return mindMap.map(this::convertToDTO).orElse(null);
        } catch (Exception e) {
            logger.error("Error fetching mind map {}: {}", mindMapId, e.getMessage());
            return null;
        }
    }

    /**
     * Toggle favorite status
     */
    public boolean toggleFavorite(String mindMapId) {
        try {
            Optional<MindMap> optionalMindMap = mindMapRepository.findById(mindMapId);
            if (optionalMindMap.isPresent()) {
                MindMap mindMap = optionalMindMap.get();
                mindMap.setIsFavorite(!mindMap.getIsFavorite());
                mindMapRepository.save(mindMap);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error toggling favorite for mind map {}: {}", mindMapId, e.getMessage());
            return false;
        }
    }

    /**
     * Delete mind map
     */
    public boolean deleteMindMap(String mindMapId) {
        try {
            if (mindMapRepository.existsById(mindMapId)) {
                mindMapRepository.deleteById(mindMapId);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error deleting mind map {}: {}", mindMapId, e.getMessage());
            return false;
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Safe extraction of videoUrl from request
     */
    private String getVideoUrl(CreateMindMapRequest request) {
        try {
            // Try using reflection to get the field value
            java.lang.reflect.Field field = request.getClass().getDeclaredField("videoUrl");
            field.setAccessible(true);
            Object value = field.get(request);
            return value != null ? value.toString() : "";
        } catch (Exception e) {
            logger.warn("Could not extract videoUrl from request: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Safe extraction of subject from request
     */
    private String getSubject(CreateMindMapRequest request) {
        try {
            // Try using reflection to get the field value
            java.lang.reflect.Field field = request.getClass().getDeclaredField("subject");
            field.setAccessible(true);
            Object value = field.get(request);
            return value != null ? value.toString() : "General";
        } catch (Exception e) {
            logger.warn("Could not extract subject from request: {}", e.getMessage());
            return "General";
        }
    }

    /**
     * Convert MindMap entity to DTO
     */
    private MindMapDTO convertToDTO(MindMap mindMap) {
        MindMapDTO dto = new MindMapDTO();
        dto.setMindMapId(mindMap.getMindMapId());
        dto.setStudentId(mindMap.getStudentId());
        dto.setVideoUrl(mindMap.getVideoUrl());
        dto.setVideoTitle(mindMap.getVideoTitle());
        dto.setSubject(mindMap.getSubject());
        dto.setStatus(mindMap.getStatus());
        dto.setMindMapJson(mindMap.getMindMapJson());
        dto.setKeyPoints(mindMap.getKeyPoints());
        dto.setSummary(mindMap.getSummary());
        dto.setIsFavorite(mindMap.getIsFavorite());
        dto.setCreatedAt(mindMap.getCreatedAt());
        dto.setUpdatedAt(mindMap.getUpdatedAt());
        return dto;
    }

    /**
     * Extract video title from URL (mock implementation)
     */
    private String extractVideoTitle(String videoUrl) {
        // Mock implementation
        if (videoUrl != null && videoUrl.contains("v=")) {
            return "Educational Video - " + videoUrl.substring(videoUrl.lastIndexOf("=") + 1);
        }
        return "Untitled Video";
    }

    /**
     * Extract mock transcript from video URL
     * NOW WITH SUBJECT-SPECIFIC CONTENT!
     * In production, integrate with YouTube API to get actual transcripts
     */
    private String extractMockTranscript(String videoUrl, String subject) {
        logger.info("Generating transcript for subject: {}", subject);
        
        // If no subject, return generic
        if (subject == null || subject.isEmpty()) {
            return generateGenericTranscript();
        }

        String subjectLower = subject.toLowerCase();

        // Match subjects and return appropriate content
        if (subjectLower.contains("biology") || subjectLower.contains("life")) {
            return "Welcome to Biology! In this lesson, we'll explore cell structure and function. The cell is the basic unit of life. " +
                    "There are two main types: prokaryotic cells (in bacteria) and eukaryotic cells (in animals and plants). " +
                    "Cell organelles include the nucleus, mitochondria, endoplasmic reticulum, and Golgi apparatus. " +
                    "The nucleus contains DNA and controls cell activities. Mitochondria are the powerhouses, producing ATP energy. " +
                    "We'll also cover photosynthesis and cellular respiration. Photosynthesis converts sunlight to chemical energy in plants. " +
                    "Cellular respiration breaks down glucose to produce ATP for all cells. " +
                    "Finally, we'll discuss genetics and DNA. DNA is the molecule of heredity. Genes code for proteins. " +
                    "Transcription converts DNA to RNA. Translation converts RNA to proteins. " +
                    "Mendel's laws explain inheritance patterns. Dominant alleles mask recessive ones.";
        } else if (subjectLower.contains("chemistry") || subjectLower.contains("chemical")) {
            return "Welcome to Chemistry! We'll study matter, atoms, and reactions. Matter exists in three states: solid, liquid, and gas. " +
                    "Atoms consist of protons (positive charge), neutrons (no charge), and electrons (negative charge). " +
                    "Protons and neutrons are in the nucleus. Electrons orbit in shells. " +
                    "The periodic table organizes elements by atomic number and properties. " +
                    "Chemical bonding occurs when atoms combine. Ionic bonds form when electrons transfer between atoms. " +
                    "Covalent bonds form when electrons are shared. Metallic bonds form between metal atoms. " +
                    "Chemical reactions occur when reactants form products. Exothermic reactions release energy. " +
                    "Endothermic reactions absorb energy. Balancing equations conserves mass. " +
                    "Acids have pH less than 7 and donate protons. Bases have pH greater than 7 and accept protons.";
        } else if (subjectLower.contains("physics") || subjectLower.contains("motion") || subjectLower.contains("energy")) {
            return "Welcome to Physics! Physics studies matter, energy, forces, and motion. " +
                    "Newton's three laws explain motion. First law: objects in motion stay in motion unless acted upon. " +
                    "Second law: Force equals mass times acceleration (F=ma). Third law: equal and opposite reactions. " +
                    "Work occurs when force acts over distance. Kinetic energy is energy of motion. " +
                    "Potential energy is stored energy. Energy is conserved and transforms between types. " +
                    "Waves transport energy through media. Sound waves are longitudinal waves. " +
                    "Frequency is cycles per unit time. Wavelength is distance between peaks. " +
                    "Electric charge can be positive or negative. Like charges repel, opposite attract. " +
                    "Current is flow of charge. Voltage is potential difference. Resistance opposes flow.";
        } else if (subjectLower.contains("math") || subjectLower.contains("calculus") || subjectLower.contains("algebra")) {
            return "Welcome to Mathematics! Today we explore key mathematical concepts. " +
                    "Algebra uses variables to represent unknown quantities. Linear equations have form y = mx + b. " +
                    "Quadratic equations have form ax² + bx + c = 0. Systems of equations can be solved by substitution or elimination. " +
                    "Geometry studies shapes, angles, and space. Triangles have three sides with angles summing to 180 degrees. " +
                    "Pythagorean theorem: a² + b² = c² for right triangles. Circles have radius and circumference πd. " +
                    "Area of circle is πr². Calculus studies change and motion. " +
                    "Derivatives measure rate of change. Integrals measure accumulation. " +
                    "Statistics deals with data. Mean is average. Median is middle value. " +
                    "Standard deviation measures spread. Probability is likelihood of events.";
        } else if (subjectLower.contains("history")) {
            return "Welcome to History! Let's explore how civilizations shaped our world. " +
                    "Ancient civilizations include Mesopotamia, Egypt, and China. " +
                    "Mesopotamia created writing and government systems. Egypt built pyramids and bureaucracy. " +
                    "Classical antiquity saw the Roman Empire expand across Europe and Mediterranean. " +
                    "Roman government included Senate and Consuls. Roman law influences modern systems. " +
                    "Medieval period featured feudalism and kingdoms. The Catholic Church dominated Europe. " +
                    "Byzantine Empire preserved Roman culture and Christianity. Islamic Golden Age advanced science and mathematics. " +
                    "Renaissance marked revival of classical learning. Humanist philosophy emphasized human potential. " +
                    "Printing press revolutionized knowledge spread. Enlightenment promoted reason and science. " +
                    "Industrial Revolution transformed society through technology.";
        } else if (subjectLower.contains("technology") || subjectLower.contains("programming")) {
            return "Welcome to Technology and Programming! We'll explore programming and computer science. " +
                    "Programming languages let us write instructions for computers. Variables store data. " +
                    "Data types include integers, floats, and strings. Control structures like if-else make decisions. " +
                    "Loops like for and while repeat code blocks. Functions are reusable code blocks. " +
                    "Parameters are function inputs. Return values are outputs. " +
                    "Object-oriented programming uses classes and objects. Inheritance lets classes inherit properties. " +
                    "Data structures organize information. Arrays store multiple values. Linked lists connect nodes. " +
                    "Stacks follow LIFO principle. Queues follow FIFO. Trees have hierarchical structure. " +
                    "Algorithms solve problems step by step. Sorting algorithms arrange data. Searching finds elements.";
        } else if (subjectLower.contains("economics") || subjectLower.contains("market")) {
            return "Welcome to Economics! Economics studies resource allocation and markets. " +
                    "Supply and demand determine prices. When demand increases, prices rise. When supply increases, prices fall. " +
                    "Equilibrium price is where supply equals demand. Market structures include competition, monopoly, and oligopoly. " +
                    "Perfect competition has many sellers with identical products. Monopoly has one seller controlling market. " +
                    "Elasticity measures quantity responsiveness to price. Macroeconomics studies overall economy. " +
                    "GDP measures total output. Inflation is price increase. Unemployment is joblessness. " +
                    "Interest rates affect borrowing and saving. Microeconomics studies consumers and producers. " +
                    "Utility is satisfaction from consumption. Profit is revenue minus costs. " +
                    "International trade benefits from comparative advantage.";
        } else {
            return generateGenericTranscript() + " Subject: " + subject;
        }
    }

    private String generateGenericTranscript() {
        return "This is a comprehensive educational transcript. " +
                "We explore fundamental concepts and principles. The foundations provide essential knowledge. " +
                "Key principles govern how the topic works. Core components interact to create systems. " +
                "Understanding relationships is crucial for mastery. Practical applications demonstrate real-world use. " +
                "Case studies show implementation in practice. Different industries use varied approaches. " +
                "Advanced topics build on fundamentals. Complex concepts require basic understanding. " +
                "Specialized applications exist for contexts. Future developments and trends are emerging. " +
                "New research expands understanding. Emerging technologies change applications. " +
                "The field continues to evolve and develop. Students learn key concepts systematically.";
    }

    /**
     * Extract key points from mind map JSON
     */
    private String extractKeyPoints(String mindMapJson) {
        // Parse JSON and extract key points
        // For now, return a structured string
        try {
            if (mindMapJson != null && !mindMapJson.isEmpty()) {
                // Mock extraction - in production, parse actual JSON
                return "1. Main Concept\n" +
                        "2. Sub-topics identified\n" +
                        "3. Key relationships mapped\n" +
                        "4. Learning objectives highlighted";
            }
        } catch (Exception e) {
            logger.error("Error extracting key points: {}", e.getMessage());
        }
        return "Key points to be extracted from mind map";
    }

    /**
     * Extract summary from mind map JSON
     */
    private String extractSummary(String mindMapJson) {
        // Parse JSON and extract summary
        // For now, return a basic summary
        try {
            if (mindMapJson != null && !mindMapJson.isEmpty()) {
                // Mock summary - in production, parse actual JSON
                return "This mind map organizes the video content into a hierarchical structure, " +
                        "helping students understand the key concepts and their relationships.";
            }
        } catch (Exception e) {
            logger.error("Error extracting summary: {}", e.getMessage());
        }
        return "Summary to be generated from mind map";
    }
}