import { Component, OnInit, OnDestroy, AfterViewInit, ElementRef, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { gsap } from 'gsap';
import { ScrollTrigger } from 'gsap/ScrollTrigger';
import * as THREE from 'three';

gsap.registerPlugin(ScrollTrigger);

@Component({
  selector: 'app-landing-page',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './landing-page.html',
  styleUrls: ['./landing-page.css']
})
export class LandingPage implements OnInit, OnDestroy, AfterViewInit {
  mobileMenuOpen = false;
  navScrolled = false;

  private deferredPrompt: any = null;
  private promptHandler: any;
  private scrollHandler: any;
  private mouseMoveHandler: any;
  private resizeHandler: any;
  private animFrameId = 0;
  private renderer!: THREE.WebGLRenderer;
  private scene!: THREE.Scene;
  private camera!: THREE.PerspectiveCamera;
  private mainMesh!: THREE.Group;
  private mouseX = 0;
  private mouseY = 0;
  private targetMouseX = 0;
  private targetMouseY = 0;
  private scrollProgress = 0;
  private destroyed = false;
  private cursorGlow!: HTMLElement;

  constructor(private router: Router, private el: ElementRef, private ngZone: NgZone) {}

  ngOnInit(): void {
    const token = localStorage.getItem('revconnect_token');
    if (token) { this.router.navigate(['/feed']); }

    this.promptHandler = (e: any) => { e.preventDefault(); this.deferredPrompt = e; };
    window.addEventListener('beforeinstallprompt', this.promptHandler);

    this.scrollHandler = () => {
      this.navScrolled = window.scrollY > 50;
      // Scroll progress bar
      const bar = this.el.nativeElement.querySelector('.scroll-progress') as HTMLElement;
      if (bar) {
        const h = document.documentElement.scrollHeight - window.innerHeight;
        bar.style.width = h > 0 ? `${(window.scrollY / h) * 100}%` : '0%';
      }
    };
    window.addEventListener('scroll', this.scrollHandler, { passive: true });
  }

  ngAfterViewInit(): void {
    this.ngZone.runOutsideAngular(() => {
      this.initCursorGlow();
      this.initThreeScene();
      this.initHeroAnimations();
      this.initScrollAnimations();
      this.initMouseParallax();
      this.initCounterAnimations();
      this.init3DTilt();
      this.initMagneticButtons();
      this.initCardGlow();
    });
  }

  ngOnDestroy(): void {
    this.destroyed = true;
    cancelAnimationFrame(this.animFrameId);
    if (this.promptHandler) window.removeEventListener('beforeinstallprompt', this.promptHandler);
    if (this.scrollHandler) window.removeEventListener('scroll', this.scrollHandler);
    if (this.mouseMoveHandler) window.removeEventListener('mousemove', this.mouseMoveHandler);
    if (this.resizeHandler) window.removeEventListener('resize', this.resizeHandler);
    if (this.cursorGlow?.parentElement) this.cursorGlow.remove();
    ScrollTrigger.getAll().forEach(t => t.kill());
    if (this.renderer) { this.renderer.dispose(); this.renderer.domElement.remove(); }
  }

  // ═══════ Cursor glow follower ═══════
  private initCursorGlow() {
    this.cursorGlow = document.createElement('div');
    this.cursorGlow.className = 'cursor-glow';
    this.el.nativeElement.querySelector('.landing-wrapper')?.appendChild(this.cursorGlow);
  }

  // ═══════════════════════════════════════════
  //  THREE.JS  —  Premium 3D Hero Scene
  // ═══════════════════════════════════════════
  private initThreeScene() {
    const canvas = this.el.nativeElement.querySelector('#hero-canvas') as HTMLCanvasElement;
    if (!canvas) return;

    this.renderer = new THREE.WebGLRenderer({ canvas, alpha: true, antialias: true });
    this.renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
    this.renderer.setSize(canvas.clientWidth, canvas.clientHeight);
    this.renderer.toneMapping = THREE.ACESFilmicToneMapping;
    this.renderer.toneMappingExposure = 1.4;

    this.scene = new THREE.Scene();

    this.camera = new THREE.PerspectiveCamera(45, canvas.clientWidth / canvas.clientHeight, 0.1, 100);
    this.camera.position.set(0, 0, 5.5);

    this.mainMesh = new THREE.Group();

    // Core icosahedron — higher detail
    const icoGeo = new THREE.IcosahedronGeometry(1.2, 2);
    const icoMat = new THREE.MeshPhysicalMaterial({
      color: 0x8b5cf6,
      metalness: 0.6,
      roughness: 0.1,
      clearcoat: 1.0,
      clearcoatRoughness: 0.03,
      transparent: true,
      opacity: 0.9,
      envMapIntensity: 2.0,
      emissive: 0x2a0845,
      emissiveIntensity: 0.15,
    });
    const icoMesh = new THREE.Mesh(icoGeo, icoMat);
    this.mainMesh.add(icoMesh);

    // Inner glow sphere
    const glowGeo = new THREE.SphereGeometry(1.0, 32, 32);
    const glowMat = new THREE.MeshBasicMaterial({
      color: 0xc084fc,
      transparent: true,
      opacity: 0.08,
    });
    const glowMesh = new THREE.Mesh(glowGeo, glowMat);
    this.mainMesh.add(glowMesh);

    // Wireframe shell
    const wireGeo = new THREE.IcosahedronGeometry(1.55, 1);
    const wireMat = new THREE.MeshBasicMaterial({
      color: 0xc084fc,
      wireframe: true,
      transparent: true,
      opacity: 0.08,
    });
    const wireMesh = new THREE.Mesh(wireGeo, wireMat);
    this.mainMesh.add(wireMesh);

    // 3 orbital rings at different angles
    const rings: THREE.Mesh[] = [];
    const ringConfigs = [
      { radius: 1.9, tube: 0.012, color: 0xc084fc, opacity: 0.3, rx: Math.PI / 2.2, ry: 0 },
      { radius: 2.15, tube: 0.008, color: 0xec4899, opacity: 0.2, rx: Math.PI / 1.7, ry: 0.5 },
      { radius: 2.4, tube: 0.006, color: 0x06b6d4, opacity: 0.12, rx: Math.PI / 3, ry: -0.3 },
    ];
    ringConfigs.forEach(c => {
      const geo = new THREE.TorusGeometry(c.radius, c.tube, 16, 120);
      const mat = new THREE.MeshBasicMaterial({ color: c.color, transparent: true, opacity: c.opacity });
      const mesh = new THREE.Mesh(geo, mat);
      mesh.rotation.x = c.rx;
      mesh.rotation.y = c.ry;
      this.mainMesh.add(mesh);
      rings.push(mesh);
    });

    // 500 floating particles in varying sizes
    const particleCount = 500;
    const pGeo = new THREE.BufferGeometry();
    const positions = new Float32Array(particleCount * 3);
    const sizes = new Float32Array(particleCount);
    for (let i = 0; i < particleCount; i++) {
      const r = 2.0 + Math.random() * 3.0;
      const theta = Math.random() * Math.PI * 2;
      const phi = Math.acos(2 * Math.random() - 1);
      positions[i * 3] = r * Math.sin(phi) * Math.cos(theta);
      positions[i * 3 + 1] = r * Math.sin(phi) * Math.sin(theta);
      positions[i * 3 + 2] = r * Math.cos(phi);
      sizes[i] = 0.01 + Math.random() * 0.03;
    }
    pGeo.setAttribute('position', new THREE.BufferAttribute(positions, 3));
    const pMat = new THREE.PointsMaterial({
      color: 0xc084fc, size: 0.025, transparent: true, opacity: 0.6,
      blending: THREE.AdditiveBlending, depthWrite: false,
    });
    const particles = new THREE.Points(pGeo, pMat);
    this.mainMesh.add(particles);

    this.scene.add(this.mainMesh);

    // Lighting — richer
    this.scene.add(new THREE.AmbientLight(0x404060, 1.0));

    const dir = new THREE.DirectionalLight(0xc084fc, 2.5);
    dir.position.set(5, 5, 5);
    this.scene.add(dir);

    const p1 = new THREE.PointLight(0xec4899, 2.0, 15);
    p1.position.set(-3, 2, 3);
    this.scene.add(p1);

    const p2 = new THREE.PointLight(0x06b6d4, 1.5, 15);
    p2.position.set(3, -2, 2);
    this.scene.add(p2);

    const p3 = new THREE.PointLight(0x8b5cf6, 1.0, 10);
    p3.position.set(0, -3, 4);
    this.scene.add(p3);

    // Scroll-linked
    ScrollTrigger.create({
      trigger: '.hero',
      start: 'top top',
      end: 'bottom top',
      scrub: 1,
      onUpdate: (self) => { this.scrollProgress = self.progress; }
    });

    // Resize
    this.resizeHandler = () => {
      if (!canvas.parentElement) return;
      const w = canvas.clientWidth;
      const h = canvas.clientHeight;
      this.camera.aspect = w / h;
      this.camera.updateProjectionMatrix();
      this.renderer.setSize(w, h);
    };
    window.addEventListener('resize', this.resizeHandler);

    // Render loop
    const clock = new THREE.Clock();
    const animate = () => {
      if (this.destroyed) return;
      this.animFrameId = requestAnimationFrame(animate);
      const t = clock.getElapsedTime();

      // Smooth mouse lerp
      this.mouseX += (this.targetMouseX - this.mouseX) * 0.05;
      this.mouseY += (this.targetMouseY - this.mouseY) * 0.05;

      // Rotation
      this.mainMesh.rotation.y = t * 0.12 + this.scrollProgress * Math.PI * 2 + this.mouseX * 0.4;
      this.mainMesh.rotation.x = Math.sin(t * 0.08) * 0.2 + this.scrollProgress * 0.6 + this.mouseY * 0.2;

      // Breathing scale
      const breathe = 1 + Math.sin(t * 0.8) * 0.03;
      const scrollScale = 1 - this.scrollProgress * 0.3;
      this.mainMesh.scale.setScalar(breathe * scrollScale);

      // Bob
      this.mainMesh.position.y = Math.sin(t * 0.4) * 0.1;

      // Ring rotations at different speeds
      rings[0].rotation.z = t * 0.25;
      rings[1].rotation.z = -t * 0.18;
      rings[2].rotation.z = t * 0.12;

      // Particles slow orbit
      particles.rotation.y = t * 0.03;
      particles.rotation.x = t * 0.015;

      // Inner glow pulse
      glowMat.opacity = 0.06 + Math.sin(t * 1.5) * 0.04;
      glowMesh.scale.setScalar(1 + Math.sin(t * 1.2) * 0.08);

      // Emissive pulse
      icoMat.emissiveIntensity = 0.1 + Math.sin(t * 0.6) * 0.08;

      // Point light orbit
      p1.position.x = Math.cos(t * 0.3) * 3;
      p1.position.z = Math.sin(t * 0.3) * 3;
      p2.position.x = Math.sin(t * 0.25) * 3;
      p2.position.z = Math.cos(t * 0.25) * 3;

      this.renderer.render(this.scene, this.camera);
    };
    animate();
  }

  // ═══════ Hero entrance — cinematic ═══════
  private initHeroAnimations() {
    const tl = gsap.timeline({ defaults: { ease: 'power3.out' } });

    tl.fromTo('#hero-canvas',
        { opacity: 0, scale: 0.7 },
        { opacity: 1, scale: 1, duration: 1.8, ease: 'expo.out' })
      .fromTo('.hero-badge',
        { y: 30, opacity: 0, scale: 0.9 },
        { y: 0, opacity: 1, scale: 1, duration: 0.7 }, '-=1.2')
      .fromTo('.hero-title',
        { y: 50, opacity: 0 },
        { y: 0, opacity: 1, duration: 1.0, ease: 'expo.out' }, '-=0.5')
      .fromTo('.hero-subtitle',
        { y: 30, opacity: 0 },
        { y: 0, opacity: 1, duration: 0.8 }, '-=0.5')
      .fromTo('.hero-actions > *',
        { y: 20, opacity: 0, scale: 0.95 },
        { y: 0, opacity: 1, scale: 1, duration: 0.6, stagger: 0.15 }, '-=0.4')
      .fromTo('.trust-item',
        { y: 15, opacity: 0 },
        { y: 0, opacity: 1, duration: 0.5, stagger: 0.1 }, '-=0.3')
      .fromTo('.hero-bg-text',
        { opacity: 0, scale: 0.9, y: 30 },
        { opacity: 1, scale: 1, y: 0, duration: 1.5, ease: 'expo.out' }, '-=1.2')
      .fromTo('.hero-glow-line',
        { scaleX: 0 },
        { scaleX: 1, duration: 1.2, ease: 'expo.out' }, '-=1.0');

    // Continuous floating animation for hero elements
    gsap.to('.hero-badge', { y: -5, duration: 2, repeat: -1, yoyo: true, ease: 'sine.inOut' });
  }

  // ═══════ Rich scroll animations ═══════
  private initScrollAnimations() {
    // Stats bar parallax
    gsap.fromTo('.stats-bar',
      { opacity: 0 },
      { opacity: 1, duration: 0.8,
        scrollTrigger: { trigger: '.stats-bar', start: 'top 92%', toggleActions: 'play none none none' }
      }
    );

    gsap.fromTo('.stat-item',
      { y: 30, opacity: 0 },
      { y: 0, opacity: 1, duration: 0.6, stagger: 0.15,
        scrollTrigger: { trigger: '.stats-bar', start: 'top 88%', toggleActions: 'play none none none' }
      }
    );

    // Section headers with clip reveal
    gsap.utils.toArray('.section-header').forEach((h: any) => {
      gsap.fromTo(h.children,
        { y: 40, opacity: 0 },
        { y: 0, opacity: 1, duration: 0.7, stagger: 0.15, ease: 'power3.out',
          scrollTrigger: { trigger: h, start: 'top 88%', toggleActions: 'play none none none' }
        }
      );
    });

    // Feature cards — staggered with rotation
    gsap.fromTo('.feature-card',
      { y: 60, opacity: 0, rotateX: -10 },
      { y: 0, opacity: 1, rotateX: 0, duration: 0.7, stagger: 0.08, ease: 'power3.out',
        scrollTrigger: { trigger: '.features-grid', start: 'top 88%', toggleActions: 'play none none none' }
      }
    );

    // Download cards — scale + slide
    gsap.fromTo('.download-card',
      { y: 50, opacity: 0, scale: 0.9 },
      { y: 0, opacity: 1, scale: 1, duration: 0.7, stagger: 0.12, ease: 'back.out(1.4)',
        scrollTrigger: { trigger: '.download-grid', start: 'top 88%', toggleActions: 'play none none none' }
      }
    );

    // Step cards — slide in from left with stagger
    gsap.fromTo('.step-card',
      { x: -40, y: 30, opacity: 0 },
      { x: 0, y: 0, opacity: 1, duration: 0.7, stagger: 0.2, ease: 'power3.out',
        scrollTrigger: { trigger: '.steps-grid', start: 'top 88%', toggleActions: 'play none none none' }
      }
    );

    gsap.fromTo('.step-connector',
      { scale: 0, opacity: 0, rotation: -90 },
      { scale: 1, opacity: 1, rotation: 0, duration: 0.4, stagger: 0.2, delay: 0.3,
        scrollTrigger: { trigger: '.steps-grid', start: 'top 88%', toggleActions: 'play none none none' }
      }
    );

    // Testimonial cards — staggered flip
    gsap.fromTo('.testimonial-card',
      { y: 50, opacity: 0, rotateY: -8 },
      { y: 0, opacity: 1, rotateY: 0, duration: 0.7, stagger: 0.12, ease: 'power3.out',
        scrollTrigger: { trigger: '.testimonials-grid', start: 'top 88%', toggleActions: 'play none none none' }
      }
    );

    // CTA — dramatic entrance
    gsap.fromTo('.cta-card',
      { y: 60, opacity: 0, scale: 0.92 },
      { y: 0, opacity: 1, scale: 1, duration: 1.0, ease: 'expo.out',
        scrollTrigger: { trigger: '.final-cta', start: 'top 88%', toggleActions: 'play none none none' }
      }
    );

    // Footer slide up
    gsap.fromTo('.landing-footer',
      { y: 30, opacity: 0 },
      { y: 0, opacity: 1, duration: 0.6,
        scrollTrigger: { trigger: '.landing-footer', start: 'top 95%', toggleActions: 'play none none none' }
      }
    );

    // Parallax effect on section backgrounds
    gsap.utils.toArray('.section-separator').forEach((sep: any) => {
      gsap.fromTo(sep, { scaleX: 0 }, {
        scaleX: 1, duration: 1,
        scrollTrigger: { trigger: sep, start: 'top 90%', toggleActions: 'play none none none' }
      });
    });
  }

  // ═══════ Mouse parallax + cursor glow ═══════
  private initMouseParallax() {
    this.mouseMoveHandler = (e: MouseEvent) => {
      this.targetMouseX = (e.clientX / window.innerWidth - 0.5) * 2;
      this.targetMouseY = (e.clientY / window.innerHeight - 0.5) * 2;

      // Cursor glow follows mouse
      if (this.cursorGlow) {
        gsap.to(this.cursorGlow, {
          x: e.clientX - 150,
          y: e.clientY + window.scrollY - 150,
          duration: 0.8,
          ease: 'power2.out',
        });
      }
    };
    window.addEventListener('mousemove', this.mouseMoveHandler);
  }

  // ═══════ Counter animation ═══════
  private initCounterAnimations() {
    const counters = this.el.nativeElement.querySelectorAll('.stat-num');
    counters.forEach((c: HTMLElement) => {
      const txt = c.textContent || '';
      const m = txt.match(/[\d.]+/);
      if (!m) return;
      const end = parseFloat(m[0]);
      const sfx = txt.replace(m[0], '');
      ScrollTrigger.create({
        trigger: c, start: 'top 90%', once: true,
        onEnter: () => {
          const o = { v: 0 };
          gsap.to(o, {
            v: end, duration: 2.5, ease: 'expo.out',
            onUpdate: () => { c.textContent = (end % 1 !== 0 ? o.v.toFixed(1) : Math.floor(o.v).toString()) + sfx; }
          });
        }
      });
    });
  }

  // ═══════ 3D tilt on cards ═══════
  private init3DTilt() {
    const cards = this.el.nativeElement.querySelectorAll('.feature-card, .download-card, .testimonial-card');
    cards.forEach((card: HTMLElement) => {
      card.addEventListener('mousemove', (e: MouseEvent) => {
        const r = card.getBoundingClientRect();
        const x = ((e.clientX - r.left) / r.width - 0.5) * 12;
        const y = ((e.clientY - r.top) / r.height - 0.5) * -12;
        gsap.to(card, { rotateY: x, rotateX: y, transformPerspective: 800, duration: 0.3, ease: 'power1.out' });
      });
      card.addEventListener('mouseleave', () => {
        gsap.to(card, { rotateY: 0, rotateX: 0, duration: 0.6, ease: 'elastic.out(1, 0.5)' });
      });
    });
  }

  // ═══════ Magnetic buttons ═══════
  private initMagneticButtons() {
    const btns = this.el.nativeElement.querySelectorAll('.btn-hero-primary, .btn-hero-secondary, .btn-primary-glow');
    btns.forEach((btn: HTMLElement) => {
      btn.addEventListener('mousemove', (e: MouseEvent) => {
        const r = btn.getBoundingClientRect();
        const x = (e.clientX - r.left - r.width / 2) * 0.3;
        const y = (e.clientY - r.top - r.height / 2) * 0.3;
        gsap.to(btn, { x, y, duration: 0.3, ease: 'power2.out' });
      });
      btn.addEventListener('mouseleave', () => {
        gsap.to(btn, { x: 0, y: 0, duration: 0.5, ease: 'elastic.out(1, 0.4)' });
      });
    });
  }

  // ═══════ Card hover glow ═══════
  private initCardGlow() {
    const cards = this.el.nativeElement.querySelectorAll('.feature-card, .download-card, .testimonial-card, .step-card');
    cards.forEach((card: HTMLElement) => {
      card.addEventListener('mousemove', (e: MouseEvent) => {
        const r = card.getBoundingClientRect();
        const x = e.clientX - r.left;
        const y = e.clientY - r.top;
        card.style.setProperty('--glow-x', `${x}px`);
        card.style.setProperty('--glow-y', `${y}px`);
      });
    });
  }

  getStarted() { this.router.navigate(['/register']); }

  scrollToSection(id: string) {
    this.mobileMenuOpen = false;
    document.getElementById(id)?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  scrollToDownload() { this.scrollToSection('download'); }

  async installPWA() {
    if (this.deferredPrompt) {
      try {
        this.deferredPrompt.prompt();
        await this.deferredPrompt.userChoice;
      } catch { /* ignore */ }
      this.deferredPrompt = null;
    } else {
      this.scrollToDownload();
    }
  }
}
