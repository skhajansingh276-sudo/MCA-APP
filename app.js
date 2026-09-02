// ── CS Department App — app.js ──

// Documents are stored in documents.json offline.


// ── Navigation History Stack ──
const navHistory = ['screen-home'];
let currentScreen = 'screen-home';
let isNavigating = false;

// ── DOM References ──
const menuBtn     = document.getElementById('menu-btn');
const closeBtn    = document.getElementById('close-btn');
const sideDrawer  = document.getElementById('side-drawer');
const menuOverlay = document.getElementById('menu-overlay');
const backBtn     = document.getElementById('back-btn');
const headerTitle = document.getElementById('header-title-text');

// ── Initialize: hide all screens except home ──
document.querySelectorAll('.screen').forEach(s => {
  if (s.id !== 'screen-home') {
    s.style.display = 'none';
  } else {
    s.style.display = 'flex';
  }
});

// ── Screen Navigation ──
function navigateTo(screenId, title, pushHistory = true) {
  if (isNavigating) return;
  const currentEl = document.getElementById(currentScreen);
  const targetEl  = document.getElementById(screenId);

  if (!targetEl || screenId === currentScreen) return;

  isNavigating = true;

  // Keep current visible with inline style during slide-out
  currentEl.style.display = 'flex';
  currentEl.classList.remove('slide-in');
  currentEl.classList.add('slide-out');

  setTimeout(() => {
    // Hide old screen
    currentEl.classList.remove('active', 'slide-out');
    currentEl.style.display = 'none';

    // Show new screen
    targetEl.style.display = 'flex';
    targetEl.classList.add('active', 'slide-in');

    // Scroll to top
    const scrollable = targetEl.querySelector('.main-content');
    if (scrollable) scrollable.scrollTop = 0;

    currentScreen = screenId;

    if (pushHistory) {
      navHistory.push(screenId);
    }

    // Update header
    updateHeader(screenId, title);
    isNavigating = false;
  }, 300);
}

function goBack() {
  if (navHistory.length <= 1 || isNavigating) return;
  navHistory.pop();
  const prevScreen = navHistory[navHistory.length - 1];

  isNavigating = true;

  const currentEl = document.getElementById(currentScreen);
  const targetEl  = document.getElementById(prevScreen);

  // Slide out current
  currentEl.style.display = 'flex';
  currentEl.classList.remove('slide-in');
  currentEl.classList.add('slide-out');

  setTimeout(() => {
    currentEl.classList.remove('active', 'slide-out');
    currentEl.style.display = 'none';

    targetEl.style.display = 'flex';
    targetEl.classList.add('active', 'slide-in');

    currentScreen = prevScreen;
    updateHeader(prevScreen);
    isNavigating = false;
  }, 300);
}

function updateHeader(screenId, title) {
  const titles = {
    'screen-home': 'MDU ROHTAK',
    'screen-semesters': 'M.Sc. CS',
    'screen-semester-detail': title || 'Semester 1',
  };

  headerTitle.textContent = titles[screenId] || title || 'MDU ROHTAK';

  // Show/hide back button
  if (screenId === 'screen-home') {
    backBtn.classList.add('hidden');
  } else {
    backBtn.classList.remove('hidden');
  }
}

// Back button
backBtn.addEventListener('click', goBack);

// ── Hamburger / Side Drawer ──
function openDrawer() {
  sideDrawer.classList.add('open');
  menuOverlay.classList.add('open');
  document.body.style.overflow = 'hidden';
}

function closeDrawer() {
  sideDrawer.classList.remove('open');
  menuOverlay.classList.remove('open');
  document.body.style.overflow = '';
}

menuBtn.addEventListener('click', openDrawer);
closeBtn.addEventListener('click', closeDrawer);
menuOverlay.addEventListener('click', closeDrawer);

// Drawer links navigation
document.querySelectorAll('.drawer-link').forEach(link => {
  link.addEventListener('click', (e) => {
    if (link.classList.contains('has-dropdown') || link.closest('.drawer-dropdown') || link.querySelector('.switch')) {
      return;
    }
    e.preventDefault();
    const screen = link.dataset.screen;
    if (!screen) return;
    closeDrawer();

    if (screen === 'home') {
      navHistory.length = 0;
      navHistory.push('screen-home');
      navigateTo('screen-home', 'CS Department', false);
    } else {
      if (currentScreen !== 'screen-home') {
        navHistory.length = 0;
        navHistory.push('screen-home');
        navigateTo('screen-home', 'CS Department', false);
        setTimeout(() => {
          const section = document.getElementById(screen);
          section?.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }, 400);
      } else {
        const section = document.getElementById(screen);
        section?.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }

      // Also update active state on bottom nav if exists
      const btn = document.getElementById('nav-' + screen);
      if (btn) {
        document.querySelectorAll('#main-bottom-nav .nav-item').forEach(item => {
          item.classList.remove('active');
        });
        btn.classList.add('active');
      }
    }
  });
});

// ── Bottom Navigation Active State (Global) ──
function setActive(btn, sectionId) {
  document.querySelectorAll('#main-bottom-nav .nav-item').forEach(item => {
    item.classList.remove('active');
  });
  btn.classList.add('active');

  if (currentScreen !== 'screen-home') {
    navHistory.length = 0;
    navHistory.push('screen-home');
    navigateTo('screen-home', 'MDU ROHTAK', false);
    
    // Allow slide animation to finish before scrolling
    setTimeout(() => {
      const section = document.getElementById(sectionId === 'home' ? 'main-scroll' : sectionId);
      if (section) {
        section.scrollIntoView({ behavior: 'smooth', block: 'start' });
      } else if (sectionId === 'home') {
         document.getElementById('main-scroll').scrollTop = 0;
      }
    }, 350);
  } else {
    const section = document.getElementById(sectionId === 'home' ? 'main-scroll' : sectionId);
    if (section) {
      section.scrollIntoView({ behavior: 'smooth', block: 'start' });
    } else if (sectionId === 'home') {
       document.getElementById('main-scroll').scrollTop = 0;
    }
  }
}

// ── Header shadow on scroll ──
const mainScroll = document.getElementById('main-scroll');
const topHeader  = document.getElementById('top-header');

if (mainScroll) {
  mainScroll.addEventListener('scroll', () => {
    if (mainScroll.scrollTop > 10) {
      topHeader.style.boxShadow = '0 4px 20px rgba(26,79,160,0.15)';
    } else {
      topHeader.style.boxShadow = '0 2px 12px rgba(26,79,160,0.07)';
    }
  });
}

let activeCourseId = 'msc';

// ── Course Card Click → Open Semesters Screen ──
document.querySelectorAll('.course-card').forEach(card => {
  card.addEventListener('click', function () {
    const course = this.dataset.course;

    // Quick press animation
    this.style.transform = 'scale(0.97)';
    setTimeout(() => {
      this.style.transform = '';
    }, 150);

    const detailTitle = document.getElementById('course-detail-title');
    const detailSub = document.getElementById('course-detail-sub');

    if (course === 'msc') {
      activeCourseId = 'msc';
      if(detailTitle) detailTitle.textContent = 'M.Sc. Computer Science';
      if(detailSub) detailSub.textContent = '2 Year Programme • 4 Semesters';
      navigateTo('screen-semesters', 'M.Sc. CS');
    } else if (course === 'mca') {
      activeCourseId = 'mca';
      if(detailTitle) detailTitle.textContent = 'MCA';
      if(detailSub) detailSub.textContent = '2 Year Programme • 4 Semesters';
      navigateTo('screen-semesters', 'MCA');
    }
  });
});

let activeSemesterNum = 1;

// ── Semester List Click → Open Course Level Syllabus ──
function openCourseSyllabus() {
  activeSemesterNum = 0; 
  console.log(`Opening Course Level Syllabus for: Course -> ${activeCourseId}`);
  
  // Use a temporary manual call to handleResourceClick with custom title
  const listContainer = document.getElementById('web-resource-list');
  const placeholderText = document.getElementById('web-resource-placeholder');
  const headerEl = document.getElementById('resource-content-header');
  
  if(headerEl) headerEl.textContent = "Course Syllabus";
  
  // Call it normally with 'Syllabus' to trigger the fetch for resource_type='syllabus'
  handleResourceClick(null, 'Syllabus', null);
}

// ── Semester Card Click → Open Semester Detail ──
function openSemester(num) {
  activeSemesterNum = num;
  console.log(`Opening specific dataset for: Course -> ${activeCourseId}, Semester -> ${num}`);
  
  // Reset practical dropdown
  const practicalDropdown = document.getElementById('practical-dropdown');
  const practicalArrow = document.getElementById('practical-arrow');
  if (practicalDropdown) practicalDropdown.classList.remove('open');
  if (practicalArrow) practicalArrow.style.transform = 'none';

  // Reset assignment dropdown
  const assignmentDropdown = document.getElementById('assignment-dropdown');
  const assignmentArrow = document.getElementById('assignment-arrow');
  if (assignmentDropdown) assignmentDropdown.classList.remove('open');
  if (assignmentArrow) assignmentArrow.style.transform = 'none';

  // Reset sessional dropdown
  const sessionalDropdown = document.getElementById('sessional-dropdown');
  const sessionalArrow = document.getElementById('sessional-arrow');
  if (sessionalDropdown) sessionalDropdown.classList.remove('open');
  if (sessionalArrow) sessionalArrow.style.transform = 'none';

  // Reset pyqs dropdown
  const pyqsDropdown = document.getElementById('pyqs-dropdown');
  const pyqsArrow = document.getElementById('pyqs-arrow');
  if (pyqsDropdown) pyqsDropdown.classList.remove('open');
  if (pyqsArrow) pyqsArrow.style.transform = 'none';

  // Update syllabus label
  const syllabusLabel = document.getElementById('syllabus-label');
  if (syllabusLabel) syllabusLabel.textContent = `Semester ${num} Syllabus`;

  navigateTo('screen-semester-detail', 'Semester ' + num);
}


function getSubjectFolders(course, sem) {
  if (course === 'mca') {
    if (sem == 1) {
      return [
        { displayName: "Computer Graphics", dbKey: "Computer_Graphics" },
        { displayName: "Oops in Java", dbKey: "Oops_Java" },
        { displayName: "Digital Design and Computer Architecture", dbKey: "Digital_Design_Arch" },
        { displayName: "Compiler Design", dbKey: "Compiler_Design" },
        { displayName: "Advanced Data Structure Using C++/Java", dbKey: "Advanced_Data_Structure" }
      ];
    } else if (sem == 4) {
      return [
        { displayName: "Advance Software Engineering", dbKey: "Advance_Software_Engineering" },
        { displayName: "IoT & Sensor Networks", dbKey: "IoT_Sensor_Networks" },
        { displayName: "Web Development Using .NET Framework", dbKey: "Web_Development_Using_DotNet" },
        { displayName: "Cyber Security & Blockchain Technology", dbKey: "Cyber_Security_Blockchain" },
        { displayName: "Neural Networks & Deep Learning", dbKey: "Neural_Networks_Deep_Learning" }
      ];
    }
  }
  return [];
}

function handleAssignmentClick(el, event) {
  if (event) event.stopPropagation();
  if (el && el.style) {
    el.style.transform = 'scale(0.97)';
    setTimeout(() => { if(el.style) el.style.transform = ''; }, 150);
  }
  
  const folders = getSubjectFolders(activeCourseId, activeSemesterNum);
  if (folders.length > 0) {
    showSubjectFoldersScreen('Assignments', 'Assignment', folders);
  } else {
    handleResourceClick(el, 'Assignments', event);
  }
}

function handleSessionalClick(el, event) {
  if (event) event.stopPropagation();
  if (el && el.style) {
    el.style.transform = 'scale(0.97)';
    setTimeout(() => { if(el.style) el.style.transform = ''; }, 150);
  }
  
  const folders = getSubjectFolders(activeCourseId, activeSemesterNum);
  if (folders.length > 0) {
    showSubjectFoldersScreen('Sessional Test', 'Sessional', folders);
  } else {
    handleResourceClick(el, 'Sessional Test', event);
  }
}

function handlePyqsClick(el, event) {
  if (event) event.stopPropagation();
  if (el && el.style) {
    el.style.transform = 'scale(0.97)';
    setTimeout(() => { if(el.style) el.style.transform = ''; }, 150);
  }
  
  if (activeCourseId === 'mca' && activeSemesterNum == 1) {
    const folders = [
      { displayName: "Bridge Course", dbKey: "bridge", isPyqOption: true },
      { displayName: "Non Bridge Course", dbKey: "non-bridge", isPyqOption: true }
    ];
    showSpecialFoldersScreen('PYQs Bank', folders);
  } else {
    const folders = getSubjectFolders(activeCourseId, activeSemesterNum);
    if (folders.length > 0) {
      showSubjectFoldersScreen('PYQs Bank', 'Pyq', folders);
    } else {
      handleResourceClick(el, 'PYQs Bank', event);
    }
  }
}

function handlePyqsSubClick(type, event) {
  if (event && event.stopPropagation) event.stopPropagation();
  if (type === 'bridge') {
    const folders = [
      { displayName: "Computer Fundamental and Programming in C", dbKey: "comp_fundamental_c" },
      { displayName: "Visual Basic", dbKey: "visual_basic" },
      { displayName: "C++ and Data Structure", dbKey: "cpp_data_structure" }
    ];
    showSubjectFoldersScreen('Bridge Course PYQs', 'PyqBridge', folders);
  } else {
    const folders = getSubjectFolders(activeCourseId, activeSemesterNum);
    showSubjectFoldersScreen('Non Bridge Course PYQs', 'Pyq', folders);
  }
}

function handlePracticalClick(el, event) {
  if (event) event.stopPropagation();
  if (el && el.style) {
    el.style.transform = 'scale(0.97)';
    setTimeout(() => { if(el.style) el.style.transform = ''; }, 150);
  }
  
  if (activeCourseId === 'mca' && activeSemesterNum == 1) {
    const folders = [
      { displayName: "Bridge Course", dbKey: "Bridge Course", isExact: true },
      { displayName: "Non Bridge Course", dbKey: "Practical Files", isExact: true }
    ];
    showSpecialFoldersScreen('Practical Files', folders);
  } else {
    handleResourceClick(el, 'Practical Files', event);
  }
}

function showSpecialFoldersScreen(title, folders) {
  const listContainer = document.getElementById('subject-folders-list');
  const headerEl = document.getElementById('subject-folders-header');
  
  if (headerEl) headerEl.textContent = title;
  if (listContainer) listContainer.innerHTML = '';
  
  folders.forEach(folder => {
    const card = document.createElement('div');
    card.style.cssText = "background: white; border-radius: 12px; padding: 16px; display: flex; align-items: center; box-shadow: 0 4px 12px rgba(0,0,0,0.06); cursor: pointer; border: 1px solid #e2e8f0; width: 100%; box-sizing: border-box; margin-bottom: 12px;";
    card.innerHTML = `
      <div style="background: #fffbeb; width: 44px; height: 44px; border-radius: 8px; display: flex; align-items: center; justify-content: center; margin-right: 16px; font-size: 20px;">📁</div>
      <div style="flex: 1;">
        <div style="font-weight: 700; color: #0f172a; font-size: 15px; font-family: 'Poppins', sans-serif;">${folder.displayName}</div>
        <div style="color: #6b7280; font-size: 13px; font-family: 'Poppins', sans-serif; font-weight: 500; margin-top: 2px;">Tap to view files</div>
      </div>
      <div style="color: #94a3b8; font-size: 24px;">›</div>
    `;
    card.onclick = () => {
      if (folder.isExact) {
        fetchAndShowResources(folder.displayName, folder.dbKey.toLowerCase());
      } else if (folder.isPyqOption) {
        handlePyqsSubClick(folder.dbKey);
      }
    };
    listContainer.appendChild(card);
  });
  
  navigateTo('screen-subject-folders', title);
}

function showSubjectFoldersScreen(title, prefix, folders, exactMatch = false) {
  const listContainer = document.getElementById('subject-folders-list');
  const headerEl = document.getElementById('subject-folders-header');
  
  if (headerEl) headerEl.textContent = title;
  if (listContainer) listContainer.innerHTML = '';
  
  folders.forEach(folder => {
    const card = document.createElement('div');
    card.style.cssText = "background: white; border-radius: 12px; padding: 16px; display: flex; align-items: center; box-shadow: 0 4px 12px rgba(0,0,0,0.06); cursor: pointer; border: 1px solid #e2e8f0; width: 100%; box-sizing: border-box;";
    card.innerHTML = `
      <div style="background: #eef2ff; width: 44px; height: 44px; border-radius: 8px; display: flex; align-items: center; justify-content: center; margin-right: 16px; font-size: 20px;">📁</div>
      <div style="flex: 1;">
        <div style="font-weight: 700; color: #0f172a; font-size: 15px; font-family: 'Poppins', sans-serif;">${folder.displayName}</div>
        <div style="color: #6b7280; font-size: 13px; font-family: 'Poppins', sans-serif; font-weight: 500; margin-top: 2px;">Tap to view files</div>
      </div>
      <div style="color: #94a3b8; font-size: 24px;">›</div>
    `;
    card.onclick = () => {
      let resourceKey = exactMatch ? folder.dbKey : `${prefix}_${folder.dbKey}`;
      fetchAndShowResources(folder.displayName, resourceKey.toLowerCase());
    };
    listContainer.appendChild(card);
  });
  
  navigateTo('screen-subject-folders', title);
}

// ── Resource Item Click & Offline Fetch ──
function fetchAndShowResources(title, dbType) {
  const headerEl = document.getElementById('resource-content-header');
  if(headerEl) headerEl.textContent = title;
  
  const listContainer = document.getElementById('web-resource-list');
  const placeholderText = document.getElementById('web-resource-placeholder');
  
  if (listContainer) listContainer.innerHTML = '';
  if (placeholderText) {
    placeholderText.style.display = 'block';
    placeholderText.textContent = "Loading offline documents...";
  }

  (async () => {
    navigateTo('screen-resource-content', title);
    
    try {
      let response;
      try {
        response = await fetch('https://raw.githubusercontent.com/skhajansingh276-sudo/MCA-APP/main/documents.json');
        if (!response.ok) throw new Error();
      } catch(e) {
        // Local fallback
        response = await fetch('./documents.json');
      }
      if (!response.ok) throw new Error("Could not fetch documents.json");
      const allDocs = await response.json();

      // Filter documents based on course, semester, and type
      const data = allDocs.filter(doc => 
        doc.course === activeCourseId &&
        doc.semester === activeSemesterNum &&
        doc.resource_type === dbType
      );

      if (listContainer) listContainer.innerHTML = '';
      
      if (data && data.length > 0) {
        data.forEach(doc => {
          const lowerUrl = (doc.file_url || "").toLowerCase();
          let iconHtml = '<div style="background: #eef2ff; width: 44px; height: 44px; border-radius: 8px; display: flex; align-items: center; justify-content: center; margin-right: 16px; font-size: 20px;">📄</div>';
          let statusText = "Tap to View Document";

          if (lowerUrl.endsWith(".jpg") || lowerUrl.endsWith(".jpeg") || lowerUrl.endsWith(".png") || lowerUrl.endsWith(".gif") || lowerUrl.endsWith(".webp")) {
            iconHtml = `<img src="${doc.file_url}" style="width: 44px; height: 44px; border-radius: 8px; object-fit: cover; margin-right: 16px; border: 1px solid #e2e8f0;">`;
            statusText = "View Image";
          } else if (lowerUrl.endsWith(".doc") || lowerUrl.endsWith(".docx")) {
            statusText = "View Word Document";
            iconHtml = '<div style="background: #eef2ff; width: 44px; height: 44px; border-radius: 8px; display: flex; align-items: center; justify-content: center; margin-right: 16px; font-size: 20px;">📝</div>';
          } else if (lowerUrl.endsWith(".pdf")) {
            statusText = "View PDF Document";
            iconHtml = '<div style="background: #fee2e2; width: 44px; height: 44px; border-radius: 8px; display: flex; align-items: center; justify-content: center; margin-right: 16px; font-size: 20px;">📕</div>';
          }

          const card = document.createElement('div');
          card.style.cssText = "background: white; border-radius: 12px; padding: 16px; display: flex; align-items: center; box-shadow: 0 4px 12px rgba(0,0,0,0.06); cursor: pointer; border: 1px solid #e2e8f0; width: 100%; box-sizing: border-box; margin-bottom: 12px;";
          card.innerHTML = `
            ${iconHtml}
            <div style="flex: 1;">
              <div style="font-weight: 700; color: #0f172a; font-size: 15px; font-family: 'Poppins', sans-serif;">${doc.title || 'Document'}</div>
              <div style="color: #1a4fa0; font-size: 13px; font-family: 'Poppins', sans-serif; font-weight: 500; margin-top: 2px;">${statusText}</div>
            </div>
            <div style="color: #94a3b8; font-size: 24px;">›</div>
          `;
          card.onclick = () => {
            const isImage = lowerUrl.endsWith(".jpg") || lowerUrl.endsWith(".jpeg") || 
                            lowerUrl.endsWith(".png") || lowerUrl.endsWith(".gif") || 
                            lowerUrl.endsWith(".webp");
            const isPdf = lowerUrl.endsWith(".pdf");
            
            if (isImage) {
              const profileModal = document.getElementById('profile-modal');
              const modalImg = document.getElementById('modal-img');
              const modalCaption = document.getElementById('modal-caption');
              if (profileModal && modalImg) {
                profileModal.style.display = "block";
                modalImg.src = doc.file_url;
                if (modalCaption) modalCaption.textContent = doc.title || "Image View";
              }
            } else if (isPdf) {
              openPdfViewer(doc.title, doc.file_url);
            } else {
              window.open(doc.file_url, '_blank');
            }
          };
          if (listContainer) listContainer.appendChild(card);
        });
        if (placeholderText) placeholderText.style.display = 'none';
      } else {
        if (placeholderText) {
          placeholderText.style.display = 'block';
          placeholderText.textContent = `No documents found yet for ${title}.`;
        }
      }
    } catch (err) {
      if (placeholderText) {
        placeholderText.style.display = 'block';
        placeholderText.textContent = `Error loading documents: ${err.message}`;
      }
    }
  })();
}

function convertGoogleDriveUrl(url) {
  if (url.includes("drive.google.com/file/d/")) {
    const parts = url.split("drive.google.com/file/d/");
    if (parts.length > 1) {
      const id = parts[1].split("/")[0];
      return `https://drive.google.com/uc?export=download&id=${id}`;
    }
  }
  return url;
}

function openPdfViewer(title, url) {
  url = convertGoogleDriveUrl(url);
  const viewerTitle = document.getElementById('pdf-viewer-title');
  if (viewerTitle) viewerTitle.textContent = title || "PDF Document";
  
  const iframe = document.getElementById('pdf-iframe');
  const spinner = document.getElementById('pdf-loading-spinner');
  
  if (iframe && spinner) {
    iframe.style.display = 'none';
    spinner.style.display = 'flex';
    
    // Use Google Docs viewer to render the PDF natively in the iframe
    iframe.src = `https://docs.google.com/viewer?url=${encodeURIComponent(url)}&embedded=true`;
    
    iframe.onload = () => {
      spinner.style.display = 'none';
      iframe.style.display = 'block';
    };
  }
  
  navigateTo('screen-pdf-viewer', title);
}



function handleResourceClick(el, name, event) {
  if (event) event.stopPropagation();
  if (el && el.style) {
    el.style.transform = 'scale(0.97)';
    setTimeout(() => { if(el.style) el.style.transform = ''; }, 150);
  }

  const dbType = name.toLowerCase().replace(/\s+/g, '_');
  fetchAndShowResources(name, dbType);
}


// ── Intersection Observer for bottom nav active state ──
const sections = {
  courses: document.getElementById('courses'),
  about:   document.getElementById('about'),
  contact: document.getElementById('contact'),
};

const navItems = {
  home:    document.getElementById('nav-home'),
  courses: document.getElementById('nav-course'),
  about:   document.getElementById('nav-help'),
  contact: document.getElementById('nav-contact'),
};

if (mainScroll) {
  const observerOptions = {
    root: mainScroll,
    threshold: 0.35,
  };

  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        const id = entry.target.id;
        Object.values(navItems).forEach(item => item?.classList.remove('active'));
        if (navItems[id]) navItems[id].classList.add('active');
      }
    });
  }, observerOptions);

  Object.values(sections).forEach(section => {
    if (section) observer.observe(section);
  });
}

// ── Profile Picture Click (Dedicated Viewer) ──
const profileModal = document.getElementById('profile-modal');
const modalImg = document.getElementById('modal-img');
const modalClose = document.getElementById('modal-close');

document.addEventListener('click', (e) => {
  if (e.target && e.target.classList.contains('profile-img')) {
    profileModal.style.display = "block";
    modalImg.src = e.target.src;
  }
});

if (modalClose) {
  modalClose.onclick = () => profileModal.style.display = "none";
}

window.onclick = (e) => {
  if (e.target == profileModal) profileModal.style.display = "none";
};

console.log('%cCS Department App Loaded ✓', 'color:#1a4fa0; font-size:14px; font-weight:bold;');

// ── Toggle Drawer Dropdown ──
function toggleDrawerDropdown(dropdownId, element) {
  const dropdown = document.getElementById(dropdownId);
  if (dropdown) {
    const isOpen = dropdown.classList.contains('open');
    
    // Close other drawer dropdowns if needed
    document.querySelectorAll('.drawer-dropdown').forEach(d => {
      if (d.id !== dropdownId) d.classList.remove('open');
    });
    document.querySelectorAll('.drawer-link.has-dropdown').forEach(el => {
      if (el !== element) el.classList.remove('expanded');
    });

    if (isOpen) {
      dropdown.classList.remove('open');
      element.classList.remove('expanded');
    } else {
      dropdown.classList.add('open');
      element.classList.add('expanded');
    }
  }
}

// ── Open Course from Drawer ──
function openCourseFromDrawer(courseId, event) {
  if (event) event.preventDefault();
  closeDrawer();
  
  const detailTitle = document.getElementById('course-detail-title');
  const detailSub = document.getElementById('course-detail-sub');

  if (courseId === 'msc') {
    activeCourseId = 'msc';
    if(detailTitle) detailTitle.textContent = 'M.Sc. Computer Science';
    if(detailSub) detailSub.textContent = '2 Year Programme • 4 Semesters';
    navigateTo('screen-semesters', 'M.Sc. CS');
  } else if (courseId === 'mca') {
    activeCourseId = 'mca';
    if(detailTitle) detailTitle.textContent = 'MCA';
    if(detailSub) detailSub.textContent = '2 Year Programme • 4 Semesters';
    navigateTo('screen-semesters', 'MCA');
  }
}

// ── Toggle Dark Mode ──
function toggleDarkMode(event) {
  if (event) event.preventDefault();
  const toggleBtn = document.getElementById('dark-mode-toggle');
  if (toggleBtn) {
    toggleBtn.checked = !toggleBtn.checked;
    if (toggleBtn.checked) {
      document.body.classList.add('dark-mode');
    } else {
      document.body.classList.remove('dark-mode');
    }
  }
}
