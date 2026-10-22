# 🌾 [KrishiSetu](https://github.com/ajaychoudhary512/KrishiSetu) — Smart Agriculture & Farm Resource Marketplace
*Bridging the gap between farmers, bio-energy industries, machinery renters, and agricultural workers to build a sustainable and efficient rural ecosystem.*

---

### 📌 Executive Summary
Modern agriculture in developing regions faces critical challenges including post-harvest stubble burning, high capital costs for farm machinery, seasonal labor shortages, and unmonitored crop health risks. **KrishiSetu** solves these challenges by providing a unified digital marketplace and AI-powered agricultural ecosystem designed to eliminate crop waste burning, streamline equipment rentals, connect farm labor, and enable secure direct-to-buyer transactions.

---

### 🏗️ System Architecture & Workflow
* **Farmer & Buyer Apps (Android):** Native mobile interface built for managing crop residue listings, equipment rentals, labor matchmaking, and AI-assisted crop scanning.
* **Spring Boot Backend / API:** Manages role-based authentication, real-time WebSocket chat, geospatial matching, and automated escrow transaction flows.
* **Persistence & AI Engine:** MongoDB Atlas for structured and unstructured agricultural records, paired with integrated computer vision machine learning models for instant crop disease detection.

---

### 🌟 Core Modules
* **1. Crop Waste Marketplace (Stubble-to-Wealth):** A direct marketplace for farmers to list organic crop residue, paddy straw, and wheat bales for purchase by bio-energy plants, paper mills, and livestock owners, turning crop waste burning into a new revenue stream.
* **2. Farm Equipment Rental & Sharing:** A pay-per-use rental model for tractors, harvesters, rotavators, and sprayers, allowing machinery owners to monetize idle equipment while small farmers gain affordable access to modern automation.
* **3. Agricultural Labor Matchmaking:** On-demand hiring for seasonal labor (sowing, harvesting, weeding, cotton picking) featuring transparent daily wage rates and local worker matching to eliminate harvest delays.
* **4. AI-Powered Crop Disease Diagnosis:** Instant leaf scan analysis using computer vision and machine learning models to deliver immediate disease diagnoses and recommended treatment remedies.
* **5. Direct Negotiation & Escrow Wallet:** Built-in real-time chat for buyer-farmer price negotiations backed by a secure escrow wallet mechanism ensuring fair and guaranteed payments upon delivery.

---

### 🌍 Impact & Sustainable Development Goals (SDGs)
* **SDG 13 (Climate Action):** Prevents toxic stubble burning emissions by establishing a circular bio-waste economy.
* **SDG 1 (No Poverty):** Increases farmer income through crop waste sales and reduced equipment operational costs.
* **SDG 8 (Decent Work & Economic Growth):** Provides fair wages, safe digital transactions, and reliable seasonal employment for rural farm workers.

---

### 🛠️ Technology Stack
* **Backend:** Spring Boot, Java, Python (API tests & scripts)
* **Persistence:** MongoDB Atlas, Spring Data MongoDB
* **Frontend:** [Native Android](https://github.com/ajaychoudhary512/KrishiSetu/tree/main/frontend), Gradle
* **AI / ML:** Computer Vision models for crop disease detection
* **Deployment & Config:** Render cloud deployment configurations (`render.yaml`)

---

### 🚀 Getting Started & Local Deployment
* **Prerequisites:** JDK 17+, Maven/Gradle wrappers, and a MongoDB Atlas cluster.
* **Environment Configuration:** Set up required properties in `local.properties` and configuration files.
* **Run Build & Tests:**
  ```bash
  # Clone repository
  git clone [https://github.com/ajaychoudhary512/KrishiSetu.git](https://github.com/ajaychoudhary512/KrishiSetu.git)
  cd KrishiSetu
  
  # Run API tests
  python test_api.py
